package local.jarios.filtro.evaluator;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.FiltroTipo;
import local.jarios.filtro.interfaces.FiltroEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
@Slf4j
public class FiltroNifsEvaluator implements FiltroEvaluator {

  private final FiltroTipo tipo = FiltroTipo.ORGANOS_CONTRATACION;

  @Override
  public boolean evaluar(Entry entry) {

    // Obtengo el conjunto de IdsPlataforma que pertenecen al filtro
    Set<String> setNifsFiltro = VariablesGlobales.getSetNifsFiltro();

    // Determino si el conjunto es null o se encuentra vacío
    if (setNifsFiltro == null || setNifsFiltro.isEmpty()) {
      log.debug("        - Filtro nifs de los órganos de contratación vacío.");
      return false;
    }

    // Obtengo el idPlataforma del Entry
    String nif = entry.getNifFromEntry();
    log.debug("        - Nif: {}", nif);

    // Determino si pertenece al conjunto que conforman el filtro
    return setNifsFiltro.contains(nif);
  }

  @Override
  public FiltroTipo getTipo() {
    return tipo;
  }
}
