package local.jarios.filtro.evaluator;

import java.time.LocalDateTime;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.FiltroTipo;
import local.jarios.filtro.interfaces.FiltroEvaluator;
import lombok.extern.slf4j.Slf4j;

/** Description: Author: juan Date: 13/10/2025 Team: */
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
  public boolean evaluar(OpenDataExecutionContext context, Entry entry) {
    LocalDateTime fechaEntry = entry.getUpdated();
    log.debug("        • Fecha entry: {}", fechaEntry);
    LocalDateTime fechaInicio = context.getFiltroFechaInicial();
    log.debug("        • Fecha inicio lectura: {}", fechaInicio);
    LocalDateTime fechaFin = context.getFiltroFechaFinal();
    log.debug("        • Fecha final lectura: {}", fechaFin);
    if (fechaEntry == null || fechaInicio == null || fechaFin == null) {
      return false;
    }

    // fechaInicio es el limite mas moderno y fechaFin el limite mas antiguo.
    return !fechaEntry.isAfter(fechaInicio) && !fechaEntry.isBefore(fechaFin);
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
