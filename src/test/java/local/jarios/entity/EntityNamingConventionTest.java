package local.jarios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.JoinTable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import local.jarios.testsupport.jpa.AbstractJpaConventionTest;
import org.junit.jupiter.api.Test;

class EntityNamingConventionTest extends AbstractJpaConventionTest {

  private static final Pattern SNAKE_CASE = Pattern.compile("^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$");

  @Test
  void allDatabaseNamesShouldBeSnakeCase() {
    List<String> errors = new ArrayList<>();

    for (Class<?> entityClass : getEntityClasses()) {
      for (Field field : getAllFields(entityClass)) {
        if (shouldSkip(field)) {
          continue;
        }

        Column column = field.getAnnotation(Column.class);
        if (column != null && hasText(column.name())) {
          validateSnakeCase(errors, entityClass, field, "@Column(name)", column.name());
        }

        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (joinColumn != null && hasText(joinColumn.name())) {
          validateSnakeCase(errors, entityClass, field, "@JoinColumn(name)", joinColumn.name());
        }

        JoinColumns joinColumns = field.getAnnotation(JoinColumns.class);
        if (joinColumns != null) {
          JoinColumn[] columns = joinColumns.value();
          for (int i = 0; i < columns.length; i++) {
            if (hasText(columns[i].name())) {
              validateSnakeCase(
                  errors, entityClass, field, "@JoinColumns[" + i + "](name)", columns[i].name());
            }
          }
        }

        JoinTable joinTable = field.getAnnotation(JoinTable.class);
        if (joinTable != null) {
          if (hasText(joinTable.name())) {
            validateSnakeCase(errors, entityClass, field, "@JoinTable(name)", joinTable.name());
          }

          JoinColumn[] joinColumnsArray = joinTable.joinColumns();
          for (int i = 0; i < joinColumnsArray.length; i++) {
            if (hasText(joinColumnsArray[i].name())) {
              validateSnakeCase(
                  errors,
                  entityClass,
                  field,
                  "@JoinTable(joinColumns[" + i + "])",
                  joinColumnsArray[i].name());
            }
          }

          JoinColumn[] inverseJoinColumns = joinTable.inverseJoinColumns();
          for (int i = 0; i < inverseJoinColumns.length; i++) {
            if (hasText(inverseJoinColumns[i].name())) {
              validateSnakeCase(
                  errors,
                  entityClass,
                  field,
                  "@JoinTable(inverseJoinColumns[" + i + "])",
                  inverseJoinColumns[i].name());
            }
          }
        }
      }
    }

    failIfErrors("Se encontraron nombres que no cumplen snake_case:", errors);
  }

  private static void validateSnakeCase(
      List<String> errors, Class<?> entityClass, Field field, String source, String value) {
    if (!SNAKE_CASE.matcher(value).matches()) {
      errors.add(
          entityClass.getName() + "." + field.getName() + " -> " + source + " = '" + value + "'");
    }
  }
}
