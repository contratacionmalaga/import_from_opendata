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
}
