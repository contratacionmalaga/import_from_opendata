package local.jarios.entity;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import local.jarios.testsupport.jpa.AbstractJpaConventionTest;
import org.junit.jupiter.api.Test;

class JpaStringLengthConsistencyTest extends AbstractJpaConventionTest {

  @Test
  void allStringFieldsShouldUseConsistentJpaAndValidationLengths() {
    List<String> errors = new ArrayList<>();

    for (Class<?> entityClass : getEntityClasses()) {
      inspectFields(entityClass, errors);
    }

    failIfErrors("Se han encontrado discrepancias de longitud en entidades JPA:", errors);
  }

  private static void inspectFields(Class<?> entityClass, List<String> errors) {
    for (Field field : getAllFields(entityClass)) {
      if (!String.class.equals(field.getType())) {
        continue;
      }

      if (shouldSkip(field)) {
        continue;
      }

      Column column = field.getAnnotation(Column.class);
      Size size = field.getAnnotation(Size.class);

      Integer columnLength = extractColumnLength(column);
      Integer sizeMax = extractSizeMax(size);

      if (columnLength != null && sizeMax != null && !columnLength.equals(sizeMax)) {
        errors.add(
            entityClass.getName()
                + "."
                + field.getName()
                + " declara @Column(length="
                + columnLength
                + ") y @Size(max="
                + sizeMax
                + ")");
      }
    }
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
}
