package local.jarios.filtro.evaluator;

import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.FiltroTipo;
import local.jarios.filtro.interfaces.FiltroEvaluator;
import lombok.extern.slf4j.Slf4j;

/** Description: Author: juan Date: 13/10/2025 Team: */
@Slf4j
public class FiltroCodigosPostalesEvaluator implements FiltroEvaluator {

  private final FiltroTipo tipo = FiltroTipo.ORGANOS_CONTRATACION;

  @Override
  public boolean evaluar(OpenDataExecutionContext context, Entry entry) {

    // Determino si el conjunto es null o se encuentra vacío
    if (context.getConjuntoNifsEnFiltro() == null || context.getConjuntoNifsEnFiltro().isEmpty()) {
      log.debug("        • Filtro códigos postales vacío.");
      return false;
    }

    // Obtengo el idPlataforma del Entry
    String nif = entry.getNifFromEntry(context.getTipoSindicacion());
    log.debug("        • Nif: {}", nif);

    // Determino si pertenece al conjunto que conforman el filtro
    return context.getConjuntoNifsEnFiltro().contains(nif);
  }

  @Override
  public FiltroTipo getTipo() {
    return tipo;
  }
}
