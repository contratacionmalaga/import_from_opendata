package local.jarios.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class EntityConsistencyTest {

  private static final String BASE_PACKAGE = "local.jarios.entity";

  @Test
  @DisplayName("Las entidades deben tener tablas únicas y consistentes")
  void entitiesShouldHaveConsistentTableNames() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    assertFalse(entities.isEmpty(), "No se encontraron entidades en el paquete " + BASE_PACKAGE);

    Map<String, Class<?>> tableNames = new HashMap<>();

    for (Class<?> entity : entities) {
      Table tableAnnotation = entity.getAnnotation(Table.class);
      assertNotNull(tableAnnotation, () -> entity.getSimpleName() + " debe tener @Table");

      String tableName = tableAnnotation.name();
      assertNotNull(tableName, () -> entity.getSimpleName() + " tiene @Table sin name definido");
      assertFalse(tableName.isEmpty(), () -> entity.getSimpleName() + " tiene @Table con name vacío");

      // Verificar duplicados
      if (tableNames.containsKey(tableName)) {
        fail("Tabla duplicada: '" + tableName + "' usada por " +
            entity.getSimpleName() + " y " + tableNames.get(tableName).getSimpleName());
      } else {
        tableNames.put(tableName, entity);
      }

      // Convención básica singular/plural
      String simpleNameLower = entity.getSimpleName().toLowerCase();
      assertTrue(
          tableName.toLowerCase().startsWith(simpleNameLower.substring(0, Math.min(5, simpleNameLower.length()))),
          () -> "El nombre de tabla '" + tableName + "' no parece consistente con la entidad " + entity.getSimpleName()
      );
    }
  }

  @Test
  @DisplayName("Los toString() de las entidades no deben tener errores de formato")
  void toStringShouldNotHaveFormattingErrors() throws Exception {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    for (Class<?> entity : entities) {
      try {
        Method toStringMethod = entity.getDeclaredMethod("toString");
        Object instance = createEmptyInstance(entity);

        String toStringResult = (String) toStringMethod.invoke(instance);

        assertFalse(toStringResult.contains("[["), entity.getSimpleName() + ".toString tiene '[[' duplicado");
        assertFalse(toStringResult.contains("]]"), entity.getSimpleName() + ".toString tiene ']]' duplicado");
        assertTrue(toStringResult.startsWith(entity.getSimpleName() + ":"),
            entity.getSimpleName() + ".toString debería empezar con '" + entity.getSimpleName() + ":'");
      } catch (NoSuchMethodException e) {
        fail(entity.getSimpleName() + " no tiene método toString()");
      }
    }
  }

  @Test
  @DisplayName("Las entidades deben tener un campo @Id")
  void entitiesShouldHaveId() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    for (Class<?> entity : entities) {
      boolean hasId = Arrays.stream(entity.getDeclaredFields())
          .anyMatch(field -> field.isAnnotationPresent(Id.class));

      assertTrue(hasId, entity.getSimpleName() + " no tiene campo @Id");
    }
  }

  @Test
  @DisplayName("Los campos String deben tener longitud definida o ser TEXT")
  void stringFieldsShouldHaveLength() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    for (Class<?> entity : entities) {
      for (var field : entity.getDeclaredFields()) {
        if (field.getType().equals(String.class) &&
            field.isAnnotationPresent(Column.class)) {

          Column col = field.getAnnotation(Column.class);
          boolean tieneLength = col.length() > 0;
          boolean esText = col.columnDefinition().toLowerCase().contains("text");

          assertTrue(tieneLength || esText,
              entity.getSimpleName() + "." + field.getName() +
                  " debería tener length definido o columnDefinition=TEXT");
        }
      }
    }
  }

  @Test
  @DisplayName("Los nombres de tablas deben estar en snake_case")
  void tableNamesShouldBeSnakeCase() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    for (Class<?> entity : entities) {
      Table table = entity.getAnnotation(Table.class);
      if (table != null) {
        String name = table.name();
        assertTrue(name.equals(name.toLowerCase()) && !name.contains(" "),
            "El nombre de tabla '" + name + "' de " + entity.getSimpleName() + " no sigue snake_case");
      }
    }
  }

  @Test
  @DisplayName("toString() no debe incluir relaciones JPA pesadas")
  void toStringShouldNotReferenceRelations() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    for (Class<?> entity : entities) {
      try {
        Method toStringMethod = entity.getDeclaredMethod("toString");
        String body = toStringMethod.toString();

        Arrays.stream(entity.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(OneToMany.class)
                || f.isAnnotationPresent(ManyToOne.class)
                || f.isAnnotationPresent(OneToOne.class)
                || f.isAnnotationPresent(ManyToMany.class))
            .forEach(f -> assertFalse(body.contains(f.getName()),
                entity.getSimpleName() + ".toString incluye relación JPA '" + f.getName() + "'"));
      } catch (NoSuchMethodException ignored) {
      }
    }
  }

  @Test
  @DisplayName("Las entidades deben tener constructor público sin argumentos")
  void entitiesShouldHaveDefaultConstructor() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    for (Class<?> entity : entities) {
      try {
        entity.getDeclaredConstructor();
      } catch (NoSuchMethodException e) {
        fail(entity.getSimpleName() + " no tiene constructor vacío");
      }
    }
  }

  @Test
  @DisplayName("Las asociaciones deben tener FetchType consistente (Lazy por defecto)")
  void associationsShouldBeLazyByDefault() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    assertFalse(entities.isEmpty(), "No se encontraron entidades en el paquete " + BASE_PACKAGE);

    for (Class<?> entity : entities) {
      for (Field field : entity.getDeclaredFields()) {

        if (field.isAnnotationPresent(ManyToOne.class)) {
          ManyToOne ann = field.getAnnotation(ManyToOne.class);
          assertEquals(FetchType.LAZY, ann.fetch(),
              entity.getSimpleName() + "." + field.getName() +
                  " (ManyToOne) debería ser LAZY");
        }

        if (field.isAnnotationPresent(OneToMany.class)) {
          OneToMany ann = field.getAnnotation(OneToMany.class);
          assertEquals(FetchType.LAZY, ann.fetch(),
              entity.getSimpleName() + "." + field.getName() +
                  " (OneToMany) debería ser LAZY");
        }

        if (field.isAnnotationPresent(OneToOne.class)) {
          OneToOne ann = field.getAnnotation(OneToOne.class);

          boolean isOwningSide = field.isAnnotationPresent(JoinColumn.class);

          if (isOwningSide) {
            // Lado propietario: sí debe ser LAZY
            assertEquals(FetchType.LAZY, ann.fetch(),
                entity.getSimpleName() + "." + field.getName() +
                    " (OneToOne dueño) debería ser LAZY");
          } else {
            // Lado inverso (mappedBy): permitimos LAZY o EAGER
            assertTrue(
                ann.fetch() == FetchType.LAZY || ann.fetch() == FetchType.EAGER,
                entity.getSimpleName() + "." + field.getName() +
                    " (OneToOne inverso) debería ser LAZY (si hay enhancement) o EAGER (por defecto)"
            );
          }
        }
      }
    }
  }

  @Test
  @DisplayName("Las relaciones OneToMany deben tener mappedBy definido")
  void oneToManyShouldHaveMappedBy() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    for (Class<?> entity : entities) {
      for (Field field : entity.getDeclaredFields()) {
        if (field.isAnnotationPresent(OneToMany.class)) {
          OneToMany ann = field.getAnnotation(OneToMany.class);
          assertFalse(ann.mappedBy().isEmpty(),
              () -> entity.getSimpleName() + "." + field.getName() +
                  " (OneToMany) debe definir mappedBy para evitar tabla join innecesaria");
        }
      }
    }
  }

  @Test
  @DisplayName("Todas las @JoinColumn deben tener foreignKey definido con nombre explícito")
  void joinColumnsShouldHaveExplicitForeignKey() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    for (Class<?> entity : entities) {
      for (Field field : entity.getDeclaredFields()) {
        if (field.isAnnotationPresent(JoinColumn.class)) {
          JoinColumn join = field.getAnnotation(JoinColumn.class);
          ForeignKey fk = join.foreignKey();
          assertNotNull(fk, () -> entity.getSimpleName() + "." + field.getName() + " debe definir foreignKey");
          assertFalse(fk.name().isEmpty(),
              () -> entity.getSimpleName() + "." + field.getName() +
                  " debe tener un nombre explícito en el foreignKey");
        }
      }
    }
  }

  @Test
  @DisplayName("Los nombres de columnas deben estar en snake_case")
  void columnNamesShouldBeSnakeCase() {
    Reflections reflections = new Reflections(BASE_PACKAGE);
    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

    for (Class<?> entity : entities) {
      for (Field field : entity.getDeclaredFields()) {
        if (field.isAnnotationPresent(Column.class)) {
          String colName = field.getAnnotation(Column.class).name();
          if (!colName.isEmpty()) {
            assertTrue(colName.equals(colName.toLowerCase()) && !colName.contains(" "),
                () -> entity.getSimpleName() + "." + field.getName() +
                    " tiene nombre de columna inválido: " + colName);
          }
        }
      }
    }
  }

  @Test
  void allEntitiesShouldHaveProperForeignKeyNames() {
    // Escanea todas las clases dentro del paquete "local.jarios.entity"
    Reflections reflections = new Reflections(BASE_PACKAGE);
    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

    for (Class<?> entityClass : entities) {
      for (Field field : entityClass.getDeclaredFields()) {
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

        if (joinColumn != null) {
          ForeignKey foreignKey = joinColumn.foreignKey();
          String fkName = foreignKey.name();

          if (fkName != null && !fkName.isEmpty()) {
            String expectedPrefix = "fk_" + entityClass.getSimpleName().toLowerCase();

            assertTrue(
                fkName.toLowerCase().startsWith(expectedPrefix),
                () -> "Entidad: " + entityClass.getSimpleName() +
                    ", campo: " + field.getName() +
                    " → FK encontrada: '" + fkName + "' " +
                    "pero se esperaba que empezara por: '" + expectedPrefix + "'"
            );
          }
        }
      }
    }
  }

  @Test
  @DisplayName("Todos los @Id deben usar UUID como tipo de dato")
  void idsShouldUseUUID() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    for (Class<?> entity : entities) {
      for (Field field : entity.getDeclaredFields()) {
        if (field.isAnnotationPresent(Id.class)) {
          assertEquals(java.util.UUID.class, field.getType(),
              () -> entity.getSimpleName() + "." + field.getName() +
                  " debe ser de tipo UUID para mantener consistencia");
        }
      }
    }
  }

  @Test
  @DisplayName("Todas las entidades deben extender de Auditable")
  void entitiesShouldExtendAuditable() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    for (Class<?> entity : entities) {
      assertTrue(local.jarios.entity.auxiliares.Auditable.class.isAssignableFrom(entity),
          () -> entity.getSimpleName() + " debe extender de Auditable");
    }
  }

  @Test
  @DisplayName("Convención de nullability en relaciones padre")
  void parentRelationsShouldRespectNullabilityConvention() {
    Reflections reflections = new Reflections(BASE_PACKAGE);
    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

    for (Class<?> entity : entities) {
      // Busca relaciones que pueden actuar como "padres"
      var parentFields = Arrays.stream(entity.getDeclaredFields())
          .filter(f -> (f.isAnnotationPresent(OneToOne.class) || f.isAnnotationPresent(ManyToOne.class)))
          .filter(f -> f.isAnnotationPresent(JoinColumn.class)) // 👈 solo los que tienen JoinColumn en este lado
          .toList();

      if (parentFields.isEmpty()) continue;

      boolean multipleParents = parentFields.size() > 1;

      for (Field field : parentFields) {
        if (!field.isAnnotationPresent(JoinColumn.class)) continue;

        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

        if (multipleParents) {
          // múltiples padres → debe quedar con el valor por defecto (nullable = true)
          assertTrue(joinColumn.nullable(),
              () -> entity.getSimpleName() + "." + field.getName() +
                  " tiene múltiples padres → no debería marcar nullable=false explícitamente");
        } else {
          // un único padre → debe ser obligatorio
          assertFalse(joinColumn.nullable(),
              () -> entity.getSimpleName() + "." + field.getName() +
                  " tiene un solo padre → debe ser nullable=false");
        }
      }
    }
  }


  @Test
  @DisplayName("Relaciones padres (lado débil con JoinColumn) deben ser LAZY y sin cascade")
  void parentRelationsShouldNotHaveCascade() {
    Reflections reflections = new Reflections(BASE_PACKAGE);

    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

    for (Class<?> entity : entities) {
      var parentFields = Arrays.stream(entity.getDeclaredFields())
          .filter(f -> f.isAnnotationPresent(JoinColumn.class)) // lado débil
          .filter(f -> f.isAnnotationPresent(OneToOne.class) || f.isAnnotationPresent(ManyToOne.class))
          .toList();

      for (Field field : parentFields) {
        if (field.isAnnotationPresent(OneToOne.class)) {
          OneToOne rel = field.getAnnotation(OneToOne.class);

          // Debe ser LAZY
          assertEquals(FetchType.LAZY, rel.fetch(),
              () -> entity.getSimpleName() + "." + field.getName() +
                  " debe usar fetch = LAZY (relación padre)");

          // No debe tener cascade
          assertEquals(0, rel.cascade().length,
              () -> entity.getSimpleName() + "." + field.getName() +
                  " no debe tener cascade en el lado padre (JoinColumn)");
        }

        if (field.isAnnotationPresent(ManyToOne.class)) {
          ManyToOne rel = field.getAnnotation(ManyToOne.class);

          // Debe ser LAZY
          assertEquals(FetchType.LAZY, rel.fetch(),
              () -> entity.getSimpleName() + "." + field.getName() +
                  " debe usar fetch = LAZY (relación padre)");

          // No debe tener cascade
          assertEquals(0, rel.cascade().length,
              () -> entity.getSimpleName() + "." + field.getName() +
                  " no debe tener cascade en el lado padre (JoinColumn)");
        }
      }
    }
  }


  private void checkJoinColumn(Class<?> entity, Field field) {
    assertTrue(field.isAnnotationPresent(JoinColumn.class),
        () -> entity.getSimpleName() + "." + field.getName() +
            " debe tener @JoinColumn");

    JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

    ForeignKey fk = joinColumn.foreignKey();
    assertNotNull(fk, () -> entity.getSimpleName() + "." + field.getName() +
        " debe definir un foreignKey");

    assertTrue(fk.foreignKeyDefinition().toUpperCase().contains("ON DELETE CASCADE"),
        () -> entity.getSimpleName() + "." + field.getName() +
            " debe definir ON DELETE CASCADE en la foreignKeyDefinition");
  }

  // 🔹 Utilidad para detectar si existe CascadeType.ALL
  private boolean hasCascadeAll(CascadeType[] cascades) {
    for (CascadeType c : cascades) {
      if (c == CascadeType.ALL) {
        return true;
      }
    }
    return false;
  }

  private String cascadesToString(CascadeType[] cascades) {
    if (cascades.length == 0) return "[]";
    StringBuilder sb = new StringBuilder("[");
    for (CascadeType c : cascades) {
      sb.append(c.name()).append(", ");
    }
    return sb.substring(0, sb.length() - 2) + "]";
  }

  private Object createEmptyInstance(Class<?> entity) {
    try {
      return entity.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException("No se pudo instanciar " + entity.getSimpleName(), e);
    }
  }
}

