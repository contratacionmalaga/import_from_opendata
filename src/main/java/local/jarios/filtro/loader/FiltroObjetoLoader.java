package local.jarios.filtro.loader;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.interfaces.FiltroLoader;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
@Slf4j
public class FiltroObjetoLoader implements FiltroLoader {

  private final PropertiesManagerService propertyManager = PropertiesManagerServiceImpl.getInstance();

  @Override
  public void cargar() throws PropertiesManagerException {
    String rawFiltroObjeto = propertyManager.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_OBJETO);
    String filtroObjeto = normalizarFiltroObjeto(rawFiltroObjeto);

    VariablesGlobales.setFiltroObjeto(filtroObjeto);
    log.debug("[FiltroObjetoLoader] - Filtro Objeto cargado: '{}'", filtroObjeto);
  }

  private String normalizarFiltroObjeto(String valor) {
    return (valor != null) ? valor.trim() : "";
  }
}
