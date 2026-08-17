package local.jarios.entity;

import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.JoinTable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import local.jarios.testsupport.jpa.AbstractJpaConventionTest;
import org.junit.jupiter.api.Test;

class EntityRelationIdentifierLengthTest extends AbstractJpaConventionTest {

  private static final int MAX_IDENTIFIER_LENGTH = 64;

  @Test
  void relationIdentifiersShouldNotExceedMaxLength() {
    List<String> errors = new ArrayList<>();

    for (Class<?> entityClass : getEntityClasses()) {
      for (Field field : getAllFields(entityClass)) {
        if (shouldSkip(field)) {
          continue;
        }

        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (joinColumn != null) {
          validateJoinColumn(errors, entityClass, field, joinColumn, "@JoinColumn");
        }

        JoinColumns joinColumns = field.getAnnotation(JoinColumns.class);
        if (joinColumns != null) {
          JoinColumn[] columns = joinColumns.value();
          for (int i = 0; i < columns.length; i++) {
            validateJoinColumn(errors, entityClass, field, columns[i], "@JoinColumns[" + i + "]");
          }
        }

        JoinTable joinTable = field.getAnnotation(JoinTable.class);
        if (joinTable != null) {
          validateLength(errors, entityClass, field, "@JoinTable(name)", joinTable.name());

          validateForeignKey(
              errors, entityClass, field, "@JoinTable(foreignKey)", joinTable.foreignKey());
          validateForeignKey(
              errors,
              entityClass,
              field,
              "@JoinTable(inverseForeignKey)",
              joinTable.inverseForeignKey());

          JoinColumn[] joinColumnsArray = joinTable.joinColumns();
          for (int i = 0; i < joinColumnsArray.length; i++) {
            validateJoinColumn(
                errors,
                entityClass,
                field,
                joinColumnsArray[i],
                "@JoinTable(joinColumns[" + i + "])");
          }

          JoinColumn[] inverseJoinColumns = joinTable.inverseJoinColumns();
          for (int i = 0; i < inverseJoinColumns.length; i++) {
            validateJoinColumn(
                errors,
                entityClass,
                field,
                inverseJoinColumns[i],
                "@JoinTable(inverseJoinColumns[" + i + "])");
          }
        }
      }
    }

    failIfErrors("Se encontraron identificadores de relación demasiado largos:", errors);
  }

  private static void validateJoinColumn(
      List<String> errors,
      Class<?> entityClass,
      Field field,
      JoinColumn joinColumn,
      String source) {
    validateLength(errors, entityClass, field, source + ".name", joinColumn.name());
    validateForeignKey(errors, entityClass, field, source + ".foreignKey", joinColumn.foreignKey());
  }

  private static void validateForeignKey(
      List<String> errors,
      Class<?> entityClass,
      Field field,
      String source,
      ForeignKey foreignKey) {
    if (foreignKey == null) {
      return;
    }

    validateLength(errors, entityClass, field, source + ".name", foreignKey.name());
  }

  private static void validateLength(
      List<String> errors, Class<?> entityClass, Field field, String source, String value) {
    if (!hasText(value)) {
      return;
    }

    if (value.length() > MAX_IDENTIFIER_LENGTH) {
      errors.add(
          entityClass.getName()
              + "."
              + field.getName()
              + " -> "
              + source
              + " = '"
              + value
              + "'"
              + " (longitud="
              + value.length()
              + ", máximo="
              + MAX_IDENTIFIER_LENGTH
              + ")");
    }
  }
}
