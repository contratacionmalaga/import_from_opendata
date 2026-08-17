package local.jarios.filtro.loader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.filtro.interfaces.FiltroLoader;
import local.jarios.helpers.StringHelper;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

/** Description: Author: juan Date: 13/10/2025 Team: */
@Slf4j
public class FiltroNifsLoader implements FiltroLoader {

  private final PropertiesManagerService propertyManager =
      PropertiesManagerServiceImpl.getInstance();

  @Override
  public void cargar(OpenDataExecutionContext context) throws PropertiesManagerException {

    // Obtengo los filtros definidos en el fichero de properties.
    String filtroNifs =
        propertyManager.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_NIFS);
    log.debug("Filtro nifs definido: '{}'", filtroNifs);

    // Limpio los espacios en blanco
    filtroNifs = (filtroNifs != null) ? filtroNifs.trim() : "";

    if (StringHelper.isValidFiltroNifs(filtroNifs)) {
      // FILTRO VÁLIDO

      // Asigno el filtro a VariablesGlobales.FiltroOcs
      context.setFiltroNifs(filtroNifs);

      // Convierto el String con la lista de códigos postales en un objeto lista
      List<String> listNif = Arrays.asList(filtroNifs.split(", "));
      log.debug("Lista de los filtros de nifs leída: {}", listNif);

      // Asigno la lista de nifs
      context.setListNifs(listNif);

      // Convierto la lista en un conjunto para facilitar la consulta de los nifs
      context.setConjuntoNifsEnFiltro(new HashSet<>(listNif));

      context.setListOrganoContratacionFiltro(
          context.getListOrganoContratacionEnExcel().stream()
              .filter(
                  oc -> {
                    String nif = oc.getNif();
                    return nif != null && context.getConjuntoNifsEnFiltro().contains(nif);
                  })
              .toList());

    } else {

      context.setFiltroNifs(null);
      log.debug("Filtro nifs NULL o BLANK. Se omite carga del filtro.");
    }
  }
}
