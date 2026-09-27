package local.jarios.core.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.properties.exception.PropertiesManagerException;
import org.junit.jupiter.api.Test;

class ImportFailureClassifierTest {

  @Test
  void classifies_database_sql_states_without_inspecting_the_message() {
    assertThat(ImportFailureClassifier.classify(new SQLException("x", "28000"), null))
        .isEqualTo(ImportErrorCategory.DATABASE_AUTHENTICATION);
    assertThat(ImportFailureClassifier.classify(new SQLException("x", "08001"), null))
        .isEqualTo(ImportErrorCategory.DATABASE_UNAVAILABLE);
  }

  @Test
  void classifies_network_errors_using_the_pipeline_phase() {
    OpenDataExecutionContext parseContext = new OpenDataExecutionContext();
    parseContext.setCurrentPhase("parse-open-data");

    assertThat(
            ImportFailureClassifier.classify(new UnknownHostException("feed.example"), parseContext))
        .isEqualTo(ImportErrorCategory.REMOTE_FEED_UNAVAILABLE);
    assertThat(ImportFailureClassifier.classify(new ConnectException("refused"), null))
        .isEqualTo(ImportErrorCategory.DATABASE_UNAVAILABLE);
  }

  @Test
  void classifies_project_exceptions_by_type() {
    assertThat(
            ImportFailureClassifier.classify(
                new PropertiesManagerException("clave obligatoria ausente"), null))
        .isEqualTo(ImportErrorCategory.CONFIGURATION_INVALID);
    assertThat(
            ImportFailureClassifier.classify(
                new MiRepositoryException("fallo persistiendo", new RuntimeException()), null))
        .isEqualTo(ImportErrorCategory.PERSISTENCE_FAILED);
  }

  @Test
  void keeps_legacy_text_as_a_last_resort() {
    assertThat(
            ImportFailureClassifier.classify(
                new IllegalStateException("Ciclo detectado en la cadena de feeds"), null))
        .isEqualTo(ImportErrorCategory.FEED_CYCLE);
  }
}

