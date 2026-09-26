package local.jarios.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Log;
import org.junit.jupiter.api.Test;

class ExecutionEmailReportBuilderTest {

  @Test
  void builds_human_success_subject_with_process_origin_and_changes() throws Exception {
    Estadistica estadistica = estadistica();

    String subject =
        ExecutionEmailReportBuilder.buildSuccessSubject(
            "con_filtros", LugarImportacion.INTERNET, estadistica);

    assertThat(subject)
        .isEqualTo(
            "Importación OpenData finalizada correctamente · con_filtros · INTERNET · 212 cambios");
  }

  @Test
  void builds_error_subject_without_malaga_reference() {
    String subject =
        ExecutionEmailReportBuilder.buildErrorSubject(
            "sin_filtros", "LOCAL", new IllegalStateException("boom"), "[RuntimeException]");

    assertThat(subject)
        .isEqualTo(
            "ERROR · Importación OpenData sin_filtros interrumpida · LOCAL · IllegalStateException")
        .doesNotContain("Malaga");
  }

  @Test
  void builds_success_body_with_summary_configuration_and_runtime() throws Exception {
    OpenDataExecutionContext context = new OpenDataExecutionContext();
    context.setLugarImportacion(LugarImportacion.INTERNET);
    context.setTipoSindicacion(TipoSindicacion.MAYORES);
    context.setFiltroFechaInicial(LocalDateTime.parse("2026-08-01T00:00:00"));
    context.setFiltroFechaFinal(LocalDateTime.parse("2026-08-19T00:00:00"));
    context.setFiltroCodigosPostales("29001, 29002");
    Estadistica estadistica = estadistica();
    context.setEstadistica(estadistica);

    String body =
        ExecutionEmailReportBuilder.buildSuccessBody(
            "import-from-opendata", "7.3.0", "con_filtros", context, estadistica);

    assertThat(body)
        .contains("Ejecución completada correctamente")
        .contains("con_filtros")
        .contains("INTERNET")
        .contains("58.076")
        .contains("212")
        .contains("Inicio lectura")
        .contains("01/08/2026 00:00:00")
        .contains("Duraci&oacute;n total")
        .contains("8m 42s");
  }

  @Test
  void builds_detailed_support_error_body_without_exposing_secrets() {
    OpenDataExecutionContext context = new OpenDataExecutionContext("C:/operacion/properties");
    context.setIncidentId("IMP-20260926-120000-12345678");
    context.setCurrentPhase("persist-open-data");
    context.setTipoSindicacion(TipoSindicacion.MAYORES);
    Throwable exception =
        new IllegalStateException(
            "No se pudo conectar: password=super-secret",
            new IllegalArgumentException("token=private-token"));

    String previousExecutionId = System.getProperty("opendata.execution.id");
    System.setProperty("opendata.execution.id", "20260926_120000_12345678");
    String body;
    try {
      body =
          ExecutionEmailReportBuilder.buildErrorBody(
              "import-from-opendata",
              "7.5.0",
              "sin_filtros",
              "LOCAL",
              context,
              exception,
              "[MiServiceException]",
              context.getIncidentId(),
              true,
              50_000);
    } finally {
      if (previousExecutionId == null) {
        System.clearProperty("opendata.execution.id");
      } else {
        System.setProperty("opendata.execution.id", previousExecutionId);
      }
    }

    assertThat(body)
        .contains("IMP-20260926-120000-12345678")
        .contains("persist-open-data")
        .contains("Pila técnica")
        .contains("logs/import-from-opendata_error_20260926_120000_12345678.log")
        .contains("[REDACTED]")
        .doesNotContain("super-secret")
        .doesNotContain("private-token");
  }

  private static Estadistica estadistica() throws Exception {
    Log log =
        new Log(
            LugarImportacion.INTERNET,
            TipoSindicacion.MAYORES,
            420,
            Date.valueOf(LocalDate.parse("2026-08-14")));

    Estadistica estadistica = new Estadistica(log);
    estadistica.setEquipo("DESKTOP-JARIOS");
    estadistica.setDuracionParseo("0h 6m 58s 0ms");
    estadistica.setDuracionPersistencia("0h 1m 44s 0ms");
    estadistica.setNumFicherosAtoms(117L);
    estadistica.setNumEntries(1284L);
    estadistica.setNumDeletedEntries(155L);
    estadistica.setNumNifsFiltro(38L);
    estadistica.setNumOrganosContratacionFiltro(42L);
    estadistica.setNumRegistrosHistoricosInsertar(64L);
    estadistica.setNumRegistrosHistoricosActualizar(148L);
    estadistica.setNumRegistrosHistoricosRechazar(57_864L);
    estadistica.setTotalHistoricos(58_076L);
    return estadistica;
  }
}
