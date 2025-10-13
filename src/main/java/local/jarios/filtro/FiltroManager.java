package local.jarios.filtro;

import local.jarios.common.util.Mensajes;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.filtro.evaluator.FiltroFechasEvaluator;
import local.jarios.filtro.evaluator.FiltroNutsEvaluator;
import local.jarios.filtro.evaluator.FiltroObjetoEvaluator;
import local.jarios.filtro.evaluator.FiltroOrganoContratacionEvaluator;
import local.jarios.filtro.loader.FiltroFechasLoader;
import local.jarios.filtro.loader.FiltroNutsLoader;
import local.jarios.filtro.loader.FiltroObjetoLoader;
import local.jarios.filtro.loader.FiltroOrganoContratacionLoader;
import local.jarios.interfaces.FiltroEvaluator;
import local.jarios.interfaces.FiltroLoader;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Description: Clase encargada de orquestar la carga y evaluación de filtros.
 */
@Slf4j
public class FiltroManager {

  private final List<FiltroLoader> loaders;
  private final List<FiltroEvaluator> evaluators;

  private final FiltroOrganoContratacionLoader ocLoader = new FiltroOrganoContratacionLoader();

  public FiltroManager() {
    this.loaders = List.of(
        new FiltroFechasLoader(),
        new FiltroNutsLoader(),
        new FiltroObjetoLoader(),
        new FiltroOrganoContratacionLoader(),
        ocLoader
    );

    this.evaluators = List.of(
        new FiltroFechasEvaluator(),
        new FiltroNutsEvaluator(),
        new FiltroObjetoEvaluator(),
        new FiltroOrganoContratacionEvaluator()
    );
  }

  public void cargarFiltros() throws PropertiesManagerException {
    for (FiltroLoader loader : loaders) {
      loader.cargar();
    }
    log.debug("[FiltroManager] - Filtros cargados correctamente");
  }

  public String evaluarFiltros(Entry entry) {
    for (FiltroEvaluator evaluator : evaluators) {
      Optional<String> resultado = evaluator.evaluar(entry);
      if (resultado.isPresent()) {
        log.debug("[FiltroManager] - {}", resultado.get());
        return resultado.get();
      }
    }
    log.debug("[FiltroManager] - Entry cumple todos los filtros.");
    return Mensajes.ENTRY_CUMPLE_FILTROS;
  }

  public List<OrganoContratacion> getListaOcs() {
    return ocLoader.getListaOcs(); // así puedes acceder desde fuera
  }
}
