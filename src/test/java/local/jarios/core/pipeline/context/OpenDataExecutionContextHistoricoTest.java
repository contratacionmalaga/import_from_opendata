package local.jarios.core.pipeline.context;

import static org.assertj.core.api.Assertions.assertThat;

import local.jarios.entity.auxiliares.Historico;
import local.jarios.enums.EntryOpcion;
import org.junit.jupiter.api.Test;

class OpenDataExecutionContextHistoricoTest {

  @Test
  void omits_rejected_historico_by_default_but_counts_it() {
    OpenDataExecutionContext context = new OpenDataExecutionContext();

    context.addHistorico(historico(EntryOpcion.RECHAZAR));

    assertThat(context.getListHistoricos()).isEmpty();
    assertThat(context.getHistoricosRechazadosOmitidos()).isEqualTo(1L);
    assertThat(context.getTotalHistoricos()).isEqualTo(1L);
  }

  @Test
  void stores_rejected_historico_when_diagnostic_mode_is_enabled() {
    OpenDataExecutionContext context = new OpenDataExecutionContext();
    context.setPersistirHistoricosRechazados(true);

    context.addHistorico(historico(EntryOpcion.RECHAZAR));

    assertThat(context.getListHistoricos()).hasSize(1);
    assertThat(context.getHistoricosRechazadosOmitidos()).isZero();
    assertThat(context.getTotalHistoricos()).isEqualTo(1L);
  }

  @Test
  void always_stores_non_rejected_historico() {
    OpenDataExecutionContext context = new OpenDataExecutionContext();

    context.addHistorico(historico(EntryOpcion.INSERTAR));

    assertThat(context.getListHistoricos()).hasSize(1);
    assertThat(context.getHistoricosRechazadosOmitidos()).isZero();
    assertThat(context.getTotalHistoricos()).isEqualTo(1L);
  }

  private static Historico historico(EntryOpcion opcion) {
    Historico historico = new Historico();
    historico.setEntryOpcion(opcion);
    return historico;
  }
}
