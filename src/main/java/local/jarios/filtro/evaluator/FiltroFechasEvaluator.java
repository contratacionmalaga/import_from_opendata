package local.jarios.filtro.evaluator;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.FiltroTipo;
import local.jarios.filtro.interfaces.FiltroEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
@Slf4j
public class FiltroFechasEvaluator implements FiltroEvaluator {

  private final FiltroTipo tipo = FiltroTipo.FECHAS;

  /**
   * Evaluar el filtro de fechas sobre un entry
   *
   * @param entry Entry sobre el que se evalua el filtro
   * @return valor devuelto
   */
  @Override
  public boolean evaluar(Entry entry) {
    LocalDateTime fechaEntry = entry.getUpdated();
    log.debug("        • Fecha entry: {}", fechaEntry);
    LocalDateTime fechaInicio = VariablesGlobales.getFiltroFechaInicial();
    log.debug("        • Fecha inicio lectura: {}", fechaInicio);
    LocalDateTime fechaFin = VariablesGlobales.getFiltroFechaFinal();
    log.debug("        • Fecha final lectura: {}", fechaFin);
    // Devuelvo TRUE
    //    si la fecha del entry NO ES NULL,
    //    si la fecha del entry NO es ANTERIOR a la fecha de Inicio
    //    si la fecha del entry NO es POSTERIOR a la fecha de Fin
    return fechaEntry != null
        && fechaEntry.isBefore(fechaInicio)
        && fechaEntry.isAfter(fechaFin);
  }

  /**
   * Tipo de filtro
   *
   * @return Tipo devuelto
   */
  @Override
  public FiltroTipo getTipo() {
    return tipo;
  }
}
