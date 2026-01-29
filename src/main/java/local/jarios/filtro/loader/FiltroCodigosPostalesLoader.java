package local.jarios.filtro.loader;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.filtro.interfaces.FiltroLoader;
import local.jarios.helpers.StringHelper;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
@Slf4j
public class FiltroCodigosPostalesLoader implements FiltroLoader {

  private final PropertiesManagerService propertyManager = PropertiesManagerServiceImpl.getInstance();

  @Getter
  private List<OrganoContratacion> listaOcsFiltro = new ArrayList<>();

  @Override
  public void cargar() throws PropertiesManagerException {

    // Obtengo los filtros definidos en el fichero de properties.
    String filtroCodigosPostales = propertyManager.getProperty(
        PropertiesFiles.FILTER, PropertiesKeys.FILTER_CODIGOS_POSTALES);
    log.debug("Filtro códigos postales definido: '{}'", filtroCodigosPostales);

    // Limpio los espacios en blanco
    filtroCodigosPostales = (filtroCodigosPostales != null) ? filtroCodigosPostales.trim() : "";

    if (StringHelper.isValidFiltroCodigosPostales(filtroCodigosPostales)) {
      // FILTRO VÁLIDO

      // Asigno el filtro a VariablesGlobales.filtroCodigosPostales
      VariablesGlobales.setFiltroCodigosPostales(filtroCodigosPostales);

      // Convierto el String con la lista de códigos postales en un objeto lista
      List<String> codigosPostalesValidos = Arrays.asList(filtroCodigosPostales.split(", "));
      log.debug(
          "Lista de los filtros de códigos postales: {}",
          codigosPostalesValidos
      );

      // Filtro la lista de órganos de contratación con aquellos que sus códigos postales que
      //    comienzan con los dos primeros dígitos de cada código postal que figura en el filtro.
      listaOcsFiltro = VariablesGlobales.getListOrganoContratacionEnExcel()
          .stream()
          .filter(oc -> {
            String cp = oc.getCodigoPostal();
            return cp != null && codigosPostalesValidos.stream().anyMatch(cp::startsWith);
          })
          .toList();
      log.debug(
          "Lista de órganos de contratación filtrada (por código postal). {} registros.",
          StringHelper.getNumeroConFormato(listaOcsFiltro.size())
      );

      // Paso los resultados a Variables Globales
      VariablesGlobales.setListOrganoContratacionFiltro(listaOcsFiltro);
      VariablesGlobales.setSetNifsFiltro(
          listaOcsFiltro.stream()
              .map(OrganoContratacion::getNif)
              .collect(Collectors.toSet())
      );
      VariablesGlobales.setListNifs(
          VariablesGlobales.getSetNifsFiltro().stream().toList()
      );

    } else {

      VariablesGlobales.setFiltroCodigosPostales(null);
      log.debug("Filtro Órganos de Contratación BLANK o NULL. Se omite carga del filtro.");
    }
  }
}
