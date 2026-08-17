package local.jarios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import local.jarios.testsupport.jpa.AbstractJpaConventionTest;
import org.junit.jupiter.api.Test;

class MapperEntityFieldCoverageTest extends AbstractJpaConventionTest {

  private static final Path PROJECT_ROOT = Paths.get("").toAbsolutePath();
  private static final Path MAPPERS_ROOT =
      PROJECT_ROOT.resolve("src/main/java/local/jarios/mappers");

  private static final Pattern DIRECT_SETTER_PATTERN =
      Pattern.compile("(\\w+)\\.set([A-Z]\\w*)\\s*\\(", Pattern.DOTALL);

  private static final Pattern METHOD_REF_SETTER_PATTERN =
      Pattern.compile("(\\w+)::set([A-Z]\\w*)", Pattern.DOTALL);

  private static final Set<String> IGNORED_FIELDS =
      Set.of("id", "createdAt", "updatedAt", "createdBy", "updatedBy");

  /** Solo entidades que realmente quieres cubrir con estos mappers. */
  private static final Set<String> COVERED_ENTITY_SIMPLE_NAMES =
      Set.of(
          "AdditionalPublicationDocumentReference",
          "AdditionalPublicationRequest",
          "AdditionalPublicationStatus",
          "AwardingCriteria",
          "ClassificationCategory",
          "ClassificationScheme",
          "CommodityClassification",
          "ContractExecutionRequirement",
          "ContractFolderStatus",
          "ContractModification",
          "DocumentReference",
          "EvaluationCriteria",
          "FinancialGuarantee",
          "NoticeInfo",
          "PreliminaryMarketConsultationStatus",
          "ProcurementProject",
          "ProcurementProjectLot",
          "TendererQualificationRequest",
          "TendererRequirement",
          "TenderingProcess",
          "TenderingTerms",
          "TenderResult");

  @Test
  void mappersShouldCoverAllRelevantEntityFields() throws Exception {
    Map<String, Class<?>> entityTypesBySimpleName = loadCoveredEntityTypesBySimpleName();
    Map<Class<?>, Set<String>> mappedFieldsByEntity = collectMappedFields(entityTypesBySimpleName);

    List<String> errors = new ArrayList<>();

    for (Class<?> entityClass : entityTypesBySimpleName.values()) {
      Set<String> expectedFields = getExpectedMappableFields(entityClass);
      Set<String> mappedFields = mappedFieldsByEntity.getOrDefault(entityClass, Set.of());

      Set<String> missing = new LinkedHashSet<>(expectedFields);
      missing.removeAll(mappedFields);

      if (!missing.isEmpty()) {
        errors.add(entityClass.getSimpleName() + " -> faltan setters para: " + missing);
      }
    }

    failIfErrors("Se han encontrado campos de entidad no cubiertos por los mappers:", errors);
  }

  @Test
  void shouldReportUnknownMappedFields() throws Exception {
    Map<String, Class<?>> entityTypesBySimpleName = loadCoveredEntityTypesBySimpleName();
    Map<Class<?>, Set<String>> mappedFieldsByEntity = collectMappedFields(entityTypesBySimpleName);

    List<String> errors = new ArrayList<>();

    for (Class<?> entityClass : entityTypesBySimpleName.values()) {
      Set<String> entityFields =
          getAllFields(entityClass).stream()
              .map(Field::getName)
              .collect(Collectors.toCollection(LinkedHashSet::new));

      Set<String> mappedFields = mappedFieldsByEntity.getOrDefault(entityClass, Set.of());

      Set<String> unknown = new LinkedHashSet<>(mappedFields);
      unknown.removeAll(entityFields);

      if (!unknown.isEmpty()) {
        errors.add(entityClass.getSimpleName() + " -> setters sin campo real: " + unknown);
      }
    }

    failIfErrors("Se han encontrado setters que no coinciden con campos reales:", errors);
  }

  private static Map<String, Class<?>> loadCoveredEntityTypesBySimpleName() {
    return getEntityClasses().stream()
        .filter(clazz -> COVERED_ENTITY_SIMPLE_NAMES.contains(clazz.getSimpleName()))
        .collect(
            Collectors.toMap(
                Class::getSimpleName, clazz -> clazz, (left, right) -> left, LinkedHashMap::new));
  }

  private static Map<Class<?>, Set<String>> collectMappedFields(
      Map<String, Class<?>> entityTypesBySimpleName) throws IOException {

    Map<Class<?>, Set<String>> mappedFieldsByEntity = new HashMap<>();

    try (Stream<Path> stream = Files.walk(MAPPERS_ROOT)) {
      List<Path> mapperFiles =
          stream.filter(path -> path.toString().endsWith(".java")).sorted().toList();

      for (Path mapperFile : mapperFiles) {
        String source = Files.readString(mapperFile);
        Map<String, String> entityVariables =
            resolveEntityVariables(source, entityTypesBySimpleName.keySet());

        collectDirectSetters(
            source, entityVariables, entityTypesBySimpleName, mappedFieldsByEntity);
        collectMethodReferenceSetters(
            source, entityVariables, entityTypesBySimpleName, mappedFieldsByEntity);
      }
    }

    return mappedFieldsByEntity;
  }

  private static void collectDirectSetters(
      String source,
      Map<String, String> entityVariables,
      Map<String, Class<?>> entityTypesBySimpleName,
      Map<Class<?>, Set<String>> mappedFieldsByEntity) {
    Matcher matcher = DIRECT_SETTER_PATTERN.matcher(source);

    while (matcher.find()) {
      String variableName = matcher.group(1);
      String setterSuffix = matcher.group(2);

      registerMappedField(
          variableName,
          setterSuffix,
          entityVariables,
          entityTypesBySimpleName,
          mappedFieldsByEntity);
    }
  }

  private static void collectMethodReferenceSetters(
      String source,
      Map<String, String> entityVariables,
      Map<String, Class<?>> entityTypesBySimpleName,
      Map<Class<?>, Set<String>> mappedFieldsByEntity) {
    Matcher matcher = METHOD_REF_SETTER_PATTERN.matcher(source);

    while (matcher.find()) {
      String variableName = matcher.group(1);
      String setterSuffix = matcher.group(2);

      registerMappedField(
          variableName,
          setterSuffix,
          entityVariables,
          entityTypesBySimpleName,
          mappedFieldsByEntity);
    }
  }

  private static void registerMappedField(
      String variableName,
      String setterSuffix,
      Map<String, String> entityVariables,
      Map<String, Class<?>> entityTypesBySimpleName,
      Map<Class<?>, Set<String>> mappedFieldsByEntity) {
    String entityTypeName = entityVariables.get(variableName);
    if (entityTypeName == null) {
      return;
    }

    Class<?> entityType = entityTypesBySimpleName.get(entityTypeName);
    if (entityType == null) {
      return;
    }

    mappedFieldsByEntity
        .computeIfAbsent(entityType, ignored -> new LinkedHashSet<>())
        .add(toFieldName(setterSuffix));
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

  private static Set<String> getExpectedMappableFields(Class<?> entityClass) {
    Set<String> fields = new LinkedHashSet<>();

    for (Field field : getAllFields(entityClass)) {
      if (shouldIgnoreForMapperCoverage(field)) {
        continue;
      }

      fields.add(field.getName());
    }

    return fields;
  }

  private static boolean shouldIgnoreForMapperCoverage(Field field) {
    if (Modifier.isStatic(field.getModifiers())
        || Modifier.isTransient(field.getModifiers())
        || field.isAnnotationPresent(Transient.class)) {
      return true;
    }

    if (IGNORED_FIELDS.contains(field.getName())) {
      return true;
    }

    if (field.isAnnotationPresent(OneToMany.class)
        || field.isAnnotationPresent(ManyToOne.class)
        || field.isAnnotationPresent(OneToOne.class)
        || field.isAnnotationPresent(ManyToMany.class)) {
      return true;
    }

    if (Collection.class.isAssignableFrom(field.getType())) {
      return true;
    }

    return !isSimplePersistableField(field);
  }

  private static boolean isSimplePersistableField(Field field) {
    Class<?> type = field.getType();

    if (type.isEnum()) {
      return true;
    }

    return type.equals(String.class)
        || type.equals(Boolean.class)
        || type.equals(Integer.class)
        || type.equals(Long.class)
        || type.equals(Double.class)
        || type.equals(java.math.BigDecimal.class)
        || type.equals(java.time.LocalDate.class)
        || type.equals(java.time.LocalDateTime.class)
        || field.isAnnotationPresent(Column.class);
  }

  private static String toFieldName(String setterSuffix) {
    return Character.toLowerCase(setterSuffix.charAt(0)) + setterSuffix.substring(1);
  }
}
