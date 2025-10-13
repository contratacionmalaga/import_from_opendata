package local.jarios.filtro.evaluator;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.FiltroTipo;
import local.jarios.interfaces.FiltroEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
@Slf4j
public class FiltroFechasEvaluator implements FiltroEvaluator {

  private final FiltroTipo tipo = FiltroTipo.FECHAS;

  @Override
  public Optional<String> evaluar(Entry entry) {
    LocalDateTime fechaEntry = entry.getUpdated();
    LocalDateTime inicio = VariablesGlobales.getFiltroFechaInicial();
    LocalDateTime fin = VariablesGlobales.getFiltroFechaFinal();

    log.debug("Fecha entry: {} | Fecha inicio lectura: {} | Fecha final lectura: {}", fechaEntry, inicio, fin);

    if (fechaEntry == null || fechaEntry.isBefore(fin) || fechaEntry.isAfter(inicio)) {
      return Optional.of(tipo.getMensajeError());
    }

    return Optional.empty();
  }

  @Override
  public FiltroTipo getTipo() {
    return tipo;
  }
}
