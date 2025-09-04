package local.jarios.util;

import local.jarios.common.util.PropertiesFiles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class PropertiesFilesTest {

  private static final Map<String, String> EXPECTED_VALUES = new HashMap<>();

  static {
    EXPECTED_VALUES.put("APP", "app");
    EXPECTED_VALUES.put("FILTER", "filter");
    EXPECTED_VALUES.put("HIBERNATE", "hibernate");
    EXPECTED_VALUES.put("JAKARTA_FILTRO", "jakarta_filtro");
    EXPECTED_VALUES.put("JAKARTA_PRINCIPAL", "jakarta_principal");
    EXPECTED_VALUES.put("MAIL", "mail");
  }

  @Test
  @DisplayName("Constantes no deben ser nulas ni vacías")
  void constantsShouldNotBeNullOrEmpty() throws IllegalAccessException {
    for (Field field : PropertiesFiles.class.getDeclaredFields()) {
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
        Object value = field.get(null);
        assertNotNull(value, () -> field.getName() + " no debe ser null");
        assertFalse(value.toString().isEmpty(), () -> field.getName() + " no debe estar vacío");
      }
    }
  }

  @Test
  @DisplayName("Constantes deben tener el valor esperado")
  void constantsShouldMatchExpectedValues() throws IllegalAccessException {
    for (Map.Entry<String, String> entry : EXPECTED_VALUES.entrySet()) {
      String fieldName = entry.getKey();
      String expectedValue = entry.getValue();

      try {
        Field field = PropertiesFiles.class.getDeclaredField(fieldName);
        String actualValue = (String) field.get(null);
        assertEquals(expectedValue, actualValue,
                     () -> "Valor incorrecto para " + fieldName);
      } catch (NoSuchFieldException e) {
        fail("Falta la constante " + fieldName + " en PropertiesFiles");
      }
    }
  }
}
