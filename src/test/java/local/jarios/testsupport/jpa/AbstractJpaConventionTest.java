package local.jarios.testsupport.jpa;

import static org.junit.jupiter.api.Assertions.fail;

import jakarta.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import local.jarios.database.EntityScanner;

public abstract class AbstractJpaConventionTest {

  private static final String BASE_PACKAGE = "local.jarios.entity";

  protected static Set<Class<?>> getEntityClasses() {
    return EntityScanner.findEntities(BASE_PACKAGE);
  }

  protected static List<Field> getAllFields(Class<?> entityClass) {
    List<Field> fields = new ArrayList<>();
    Class<?> current = entityClass;

    while (current != null && current != Object.class) {
      Collections.addAll(fields, current.getDeclaredFields());
      current = current.getSuperclass();
    }

    return fields;
  }

  protected static boolean shouldSkip(Field field) {
    return Modifier.isStatic(field.getModifiers())
        || Modifier.isTransient(field.getModifiers())
        || field.isAnnotationPresent(Transient.class);
  }

  protected static boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  protected static void failIfErrors(String header, List<String> errors) {
    if (errors.isEmpty()) {
      return;
    }

    errors.sort(Comparator.naturalOrder());

    StringBuilder message = new StringBuilder(header).append("\n\n");
    for (String error : errors) {
      message.append("- ").append(error).append('\n');
    }

    fail(message.toString());
  }
}
