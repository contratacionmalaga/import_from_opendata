package local.jarios.core.abstracts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AbstractOpenDataBaseEmailConfigurationTest {

  @Test
  void keeps_email_enabled_when_the_optional_property_is_missing_or_not_false() {
    assertThat(AbstractOpenDataBase.isEmailNotificationEnabled(null)).isTrue();
    assertThat(AbstractOpenDataBase.isEmailNotificationEnabled(" ")).isTrue();
    assertThat(AbstractOpenDataBase.isEmailNotificationEnabled("true")).isTrue();
    assertThat(AbstractOpenDataBase.isEmailNotificationEnabled("unexpected")).isTrue();
  }

  @Test
  void disables_email_only_when_explicitly_configured_as_false() {
    assertThat(AbstractOpenDataBase.isEmailNotificationEnabled("false")).isFalse();
    assertThat(AbstractOpenDataBase.isEmailNotificationEnabled(" FALSE ")).isFalse();
  }

  @Test
  void translates_database_authentication_failures_into_an_operator_message() {
    Throwable exception =
        new IllegalStateException(
            "Error creando SessionFactory",
            new RuntimeException("Access denied for user 'root'@'127.0.0.1'"));

    assertThat(AbstractOpenDataBase.getOperatorMessage(exception))
        .isEqualTo(
            "No se ha podido conectar a la base de datos: las credenciales configuradas han sido rechazadas. Revise el usuario y la contraseña de bd.properties.");
  }

  @Test
  void translates_known_import_failures_into_operator_messages() {
    assertThat(
            AbstractOpenDataBase.getOperatorMessage(
                new IllegalStateException("Ya existe una importacion en curso para opendata:MAYORES")))
        .contains("otra ejecución en curso");
    assertThat(
            AbstractOpenDataBase.getOperatorMessage(
                new IllegalStateException("Enlace next informado pero no válido: feed.atom")))
        .contains("enlace al siguiente feed");
    assertThat(
            AbstractOpenDataBase.getOperatorMessage(
                new IllegalStateException("Ciclo detectado en la cadena de feeds: feed.atom")))
        .contains("ciclo entre feeds");
    assertThat(
            AbstractOpenDataBase.getOperatorMessage(
                new IllegalStateException("No se ha podido leer el XML ATOM de forma segura")))
        .contains("XML ATOM no es válido");
  }
}
