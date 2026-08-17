package local.jarios.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import local.jarios.common.util.TamanoCampos;
import local.jarios.testsupport.jpa.AbstractJpaConventionTest;
import org.junit.jupiter.api.Test;

class MapperEntityStringLengthConsistencyTest extends AbstractJpaConventionTest {

  private static final Path PROJECT_ROOT = Paths.get("").toAbsolutePath();
  private static final Path MAPPERS_ROOT =
      PROJECT_ROOT.resolve("src/main/java/local/jarios/mappers");

  private static final Pattern DIRECT_SETTER_PATTERN =
      Pattern.compile("(\\w+)\\.set([A-Z]\\w*)\\((.*?)\\);", Pattern.DOTALL);
  private static final Pattern HELPER_SETTER_PATTERN =
      Pattern.compile("setLimited\\(\\s*(\\w+)::set([A-Z]\\w*)\\s*,(.*?)\\)", Pattern.DOTALL);
  private static final Pattern IF_PRESENT_SETTER_PATTERN =
      Pattern.compile("\\.ifPresent\\((\\w+)::set([A-Z]\\w*)\\);", Pattern.DOTALL);
  private static final Pattern TEMP_LIMIT_PATTERN =
      Pattern.compile(
          "String\\s+(\\w+)\\s*=\\s*StringHelper\\.(?:limit|normalizeAndLimit)\\((.*?)\\);",
          Pattern.DOTALL);

  @Test
  void mapperStringLimitsShouldNotExceedDestinationFieldCapacity() throws Exception {
    Map<String, Class<?>> entityTypesBySimpleName = loadEntityTypesBySimpleName();
    List<MapperAssignment> assignments = parseMapperAssignments(entityTypesBySimpleName);

    assertFalse(
        assignments.isEmpty(), "No se han encontrado asignaciones String limitadas en mappers");

    List<String> errors = new ArrayList<>();
    for (MapperAssignment assignment : assignments) {
      Integer entityLimit =
          resolveEntityFieldLimit(assignment.entityType(), assignment.fieldName());
      if (entityLimit == null) {
        continue;
      }

      if (assignment.maxLength() > entityLimit) {
        errors.add(
            assignment.mapperPath()
                + " -> "
                + assignment.entityType().getSimpleName()
                + "."
                + assignment.fieldName()
                + " limita a "
                + assignment.maxLength()
                + " pero la entidad permite "
                + entityLimit);
      }
    }

    failIfErrors(
        "Se han encontrado discrepancias entre límites de mappers y entidades JPA:", errors);
  }

  @Test
  void shouldReportCurrentMapperLengthDiscrepanciesForReview() throws Exception {
    Map<String, Class<?>> entityTypesBySimpleName = loadEntityTypesBySimpleName();
    List<MapperAssignment> assignments = parseMapperAssignments(entityTypesBySimpleName);

    List<String> discrepancies =
        assignments.stream()
            .map(
                assignment -> {
                  Integer entityLimit =
                      resolveEntityFieldLimit(assignment.entityType(), assignment.fieldName());
                  if (entityLimit == null || assignment.maxLength() <= entityLimit) {
                    return null;
                  }

                  return assignment.mapperPath()
                      + " :: "
                      + assignment.entityType().getSimpleName()
                      + "."
                      + assignment.fieldName()
                      + " :: mapper="
                      + assignment.maxLength()
                      + ", entity="
                      + entityLimit;
                })
            .filter(Objects::nonNull)
            .sorted()
            .toList();

    if (!discrepancies.isEmpty()) {
      System.out.println("Informe de discrepancias mapper-entidad:");
      discrepancies.forEach(line -> System.out.println(" - " + line));
    }
  }

  private static Map<String, Class<?>> loadEntityTypesBySimpleName() {
    Set<Class<?>> entityClasses = getEntityClasses();

    return entityClasses.stream()
        .collect(
            Collectors.toMap(
                Class::getSimpleName, clazz -> clazz, (left, right) -> left, LinkedHashMap::new));
  }

  private static List<MapperAssignment> parseMapperAssignments(
      Map<String, Class<?>> entityTypesBySimpleName) throws IOException {

    List<MapperAssignment> assignments = new ArrayList<>();

    try (Stream<Path> stream = Files.walk(MAPPERS_ROOT)) {
      List<Path> mapperFiles =
          stream.filter(path -> path.toString().endsWith(".java")).sorted().toList();

      for (Path mapperFile : mapperFiles) {
        String source = Files.readString(mapperFile);
        Map<String, String> entityVariables =
            resolveEntityVariables(source, entityTypesBySimpleName.keySet());
        Map<String, Integer> tempStringLimits = resolveTempStringLimits(source);

        collectDirectSetterAssignments(
            mapperFile,
            source,
            entityVariables,
            entityTypesBySimpleName,
            tempStringLimits,
            assignments);
        collectHelperSetterAssignments(
            mapperFile, source, entityVariables, entityTypesBySimpleName, assignments);
        collectIfPresentSetterAssignments(
            mapperFile, source, entityVariables, entityTypesBySimpleName, assignments);
      }
    }

    return assignments;
  }

  private static Map<String, String> resolveEntityVariables(
      String source, Collection<String> entityTypeNames) {
    Map<String, String> variables = new HashMap<>();

    for (String entityTypeName : entityTypeNames) {
      Pattern pattern = Pattern.compile("\\b" + Pattern.quote(entityTypeName) + "\\s+(\\w+)\\b");
      Matcher matcher = pattern.matcher(source);
      while (matcher.find()) {
        variables.put(matcher.group(1), entityTypeName);
      }
    }

    return variables;
  }

  private static Map<String, Integer> resolveTempStringLimits(String source) {
    Map<String, Integer> limits = new HashMap<>();
    Matcher matcher = TEMP_LIMIT_PATTERN.matcher(source);

    while (matcher.find()) {
      Integer maxLength = extractTamanoConstantValue(matcher.group(2));
      if (maxLength != null) {
        limits.put(matcher.group(1), maxLength);
      }
    }

    return limits;
  }

  private static void collectDirectSetterAssignments(
      Path mapperFile,
      String source,
      Map<String, String> entityVariables,
      Map<String, Class<?>> entityTypesBySimpleName,
      Map<String, Integer> tempStringLimits,
      List<MapperAssignment> assignments) {
    Matcher matcher = DIRECT_SETTER_PATTERN.matcher(source);

    while (matcher.find()) {
      String variableName = matcher.group(1);
      String setterSuffix = matcher.group(2);
      String argumentExpression = matcher.group(3);

      String entityTypeName = entityVariables.get(variableName);
      if (entityTypeName == null) {
        continue;
      }

      Integer maxLength = extractTamanoConstantValue(argumentExpression);
      if (maxLength == null) {
        maxLength = tempStringLimits.get(argumentExpression.trim());
      }

      if (maxLength == null) {
        continue;
      }

      Class<?> entityType = entityTypesBySimpleName.get(entityTypeName);
      if (entityType == null) {
        continue;
      }

      assignments.add(
          new MapperAssignment(
              PROJECT_ROOT.relativize(mapperFile),
              entityType,
              toFieldName(setterSuffix),
              maxLength));
    }
  }

  private static void collectHelperSetterAssignments(
      Path mapperFile,
      String source,
      Map<String, String> entityVariables,
      Map<String, Class<?>> entityTypesBySimpleName,
      List<MapperAssignment> assignments) {
    Matcher matcher = HELPER_SETTER_PATTERN.matcher(source);

    while (matcher.find()) {
      String variableName = matcher.group(1);
      String setterSuffix = matcher.group(2);
      String fullExpression = matcher.group(0);

      String entityTypeName = entityVariables.get(variableName);
      if (entityTypeName == null) {
        continue;
      }

      Integer maxLength = extractTamanoConstantValue(fullExpression);
      if (maxLength == null) {
        continue;
      }

      Class<?> entityType = entityTypesBySimpleName.get(entityTypeName);
      if (entityType == null) {
        continue;
      }

      assignments.add(
          new MapperAssignment(
              PROJECT_ROOT.relativize(mapperFile),
              entityType,
              toFieldName(setterSuffix),
              maxLength));
    }
  }

  private static void collectIfPresentSetterAssignments(
      Path mapperFile,
      String source,
      Map<String, String> entityVariables,
      Map<String, Class<?>> entityTypesBySimpleName,
      List<MapperAssignment> assignments) {
    Matcher matcher = IF_PRESENT_SETTER_PATTERN.matcher(source);

    while (matcher.find()) {
      String variableName = matcher.group(1);
      String setterSuffix = matcher.group(2);

      String entityTypeName = entityVariables.get(variableName);
      if (entityTypeName == null) {
        continue;
      }

      int snippetStart = Math.max(0, matcher.start() - 400);
      String snippet = source.substring(snippetStart, matcher.end());
      Integer maxLength = extractTamanoConstantValue(snippet);
      if (maxLength == null) {
        continue;
      }

      Class<?> entityType = entityTypesBySimpleName.get(entityTypeName);
      if (entityType == null) {
        continue;
      }

      assignments.add(
          new MapperAssignment(
              PROJECT_ROOT.relativize(mapperFile),
              entityType,
              toFieldName(setterSuffix),
              maxLength));
    }
  }

  private static Integer resolveEntityFieldLimit(Class<?> entityType, String fieldName) {
    Field field = findField(entityType, fieldName);
    if (field == null || !String.class.equals(field.getType())) {
      return null;
    }

    Integer columnLength = extractColumnLength(field.getAnnotation(Column.class));
    Integer sizeMax = extractSizeMax(field.getAnnotation(Size.class));

    if (columnLength != null && sizeMax != null) {
      return Math.min(columnLength, sizeMax);
    }

    return columnLength != null ? columnLength : sizeMax;
  }

  private static Field findField(Class<?> entityType, String fieldName) {
    Class<?> current = entityType;
    while (current != null && current != Object.class) {
      try {
        return current.getDeclaredField(fieldName);
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      }
    }
    return null;
  }

  private static Integer extractColumnLength(Column column) {
    if (column == null) {
      return null;
    }

    String columnDefinition = column.columnDefinition();
    if (columnDefinition != null && columnDefinition.toUpperCase(Locale.ROOT).contains("TEXT")) {
      return null;
    }

    return column.length();
  }

  private static Integer extractSizeMax(Size size) {
    if (size == null || size.max() == Integer.MAX_VALUE) {
      return null;
    }

    return size.max();
  }

  private static Integer extractTamanoConstantValue(String text) {
    for (Field field : TamanoCampos.class.getDeclaredFields()) {
      if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
        continue;
      }

      String constantName = field.getName();
      if (!text.contains("TamanoCampos." + constantName)) {
        continue;
      }

      try {
        return field.getInt(null);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("No se pudo leer la constante " + constantName, e);
      }
    }

    return null;
  }

  private static String toFieldName(String setterSuffix) {
    return Character.toLowerCase(setterSuffix.charAt(0)) + setterSuffix.substring(1);
  }

  private record MapperAssignment(
      Path mapperPath, Class<?> entityType, String fieldName, int maxLength) {}
}
