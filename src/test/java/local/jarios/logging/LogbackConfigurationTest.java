package local.jarios.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LogbackConfigurationTest {

  @Test
  void keeps_stack_traces_out_of_console_and_in_the_dedicated_error_log() throws Exception {
    try (InputStream resource = getClass().getResourceAsStream("/logback.xml")) {
      assertThat(resource).isNotNull();
      String xml = new String(resource.readAllBytes(), StandardCharsets.UTF_8);

      assertThat(xml)
          .contains("name=\"ERROR_FILE\"")
          .contains("${APP_NAME}_error_${EXECUTION_ID}.log")
          .contains("opendata.execution.id")
          .doesNotContain("%X{incidentId}")
          .contains("%ex{full}")
          .contains("%nopex")
          .contains("name=\"CONSOLE_ERROR\"")
          .contains("name=\"local.jarios.core.abstracts.AbstractOpenDataBase\"");
    }
  }
}
