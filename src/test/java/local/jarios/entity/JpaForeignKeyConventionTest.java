package local.jarios.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SecondaryTables;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import local.jarios.testsupport.jpa.AbstractJpaConventionTest;
import org.junit.jupiter.api.Test;

class JpaForeignKeyConventionTest extends AbstractJpaConventionTest {

  private static final int MAX_IDENTIFIER_LENGTH = 64;

  @Test
  void allForeignKeysShouldHaveExplicitNamesAndRespectLengthLimit() {
    List<String> errors = new ArrayList<>();

    for (Class<?> entityClass : getEntityClasses()) {
      inspectEntityLevelAnnotations(entityClass, errors);
      inspectFields(entityClass, errors);
    }

    failIfErrors("Se han encontrado problemas en la convención de foreign keys JPA:", errors);
  }

  private static void inspectEntityLevelAnnotations(Class<?> entityClass, List<String> errors) {
    SecondaryTable secondaryTable = entityClass.getAnnotation(SecondaryTable.class);
    if (secondaryTable != null) {
      validateForeignKey(entityClass, "@SecondaryTable", secondaryTable.foreignKey(), errors);
    }

    SecondaryTables secondaryTables = entityClass.getAnnotation(SecondaryTables.class);
    if (secondaryTables != null) {
      SecondaryTable[] tables = secondaryTables.value();
      for (int i = 0; i < tables.length; i++) {
        validateForeignKey(
            entityClass, "@SecondaryTables[" + i + "]", tables[i].foreignKey(), errors);
      }
    }
  }

  private static void inspectFields(Class<?> entityClass, List<String> errors) {
    for (Field field : getAllFields(entityClass)) {
      if (shouldSkip(field)) {
        continue;
      }

      JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
      if (joinColumn != null) {
        validateForeignKey(
            entityClass,
            "campo '" + field.getName() + "' @JoinColumn",
            joinColumn.foreignKey(),
            errors);
      }

      JoinColumns joinColumns = field.getAnnotation(JoinColumns.class);
      if (joinColumns != null) {
        JoinColumn[] columns = joinColumns.value();
        for (int i = 0; i < columns.length; i++) {
          validateForeignKey(
              entityClass,
              "campo '" + field.getName() + "' @JoinColumns[" + i + "]",
              columns[i].foreignKey(),
              errors);
        }
      }

      CollectionTable collectionTable = field.getAnnotation(CollectionTable.class);
      if (collectionTable != null) {
        validateForeignKey(
            entityClass,
            "campo '" + field.getName() + "' @CollectionTable",
            collectionTable.foreignKey(),
            errors);
      }
    }
  }

  private static void validateForeignKey(
      Class<?> entityClass, String location, ForeignKey foreignKey, List<String> errors) {
    if (foreignKey == null) {
      return;
    }

    String name = foreignKey.name();

    if (!hasText(name)) {
      errors.add(
          entityClass.getName()
              + " -> "
              + location
              + " no tiene nombre explícito en @ForeignKey(name = ...)");
      return;
    }

    if (name.length() > MAX_IDENTIFIER_LENGTH) {
      errors.add(
          entityClass.getName()
              + " -> "
              + location
              + " usa la FK '"
              + name
              + "' con longitud "
              + name.length()
              + " y supera el máximo de "
              + MAX_IDENTIFIER_LENGTH);
    }
  }
}
