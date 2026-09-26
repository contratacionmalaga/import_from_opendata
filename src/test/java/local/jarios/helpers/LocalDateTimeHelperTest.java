package local.jarios.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LocalDateTimeHelperTest {

  @Test
  void formats_monotonic_duration_in_milliseconds() {
    assertThat(LocalDateTimeHelper.getDiferenciaNanos(0, 1_500_000_000L))
        .isEqualTo("0h 0m 1s 500ms");
  }

  @Test
  void clamps_negative_monotonic_duration_to_zero() {
    assertThat(LocalDateTimeHelper.getDiferenciaNanos(10, 0)).isEqualTo("0h 0m 0s 0ms");
  }
}
