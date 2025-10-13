package local.jarios.filtro.loader;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.entity.auxiliares.Provincia;
import local.jarios.helpers.StringHelper;
import local.jarios.interfaces.FiltroLoader;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.services.ServiceFiltro;
import local.jarios.services.ServiceFiltroImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
@Slf4j
public class FiltroOrganoContratacionLoader implements FiltroLoader {

  private final PropertiesManagerService propertyManager = PropertiesManagerServiceImpl.getInstance();

  @Getter
  private List<OrganoContratacion> listaOcs = new ArrayList<>();

  @Override
  public void cargar() throws PropertiesManagerException {
    String filtroOcs = propertyManager.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_ORGANOS_CONTRATACION);
    filtroOcs = (filtroOcs != null) ? filtroOcs.trim() : "";

    if (StringHelper.isValidString(filtroOcs)) {
      // El filtro es válido

      // Asigno el filtro a VariablesGlobales.FiltroOcs
      VariablesGlobales.setFiltroOcs(filtroOcs);
      log.debug("[FiltroSqlLoader] - Filtro OCs definido: '{}'", filtroOcs);

      // Creo el objeto para la conexión con la base de datos y obtener la lista de OCs
      ServiceFiltro service = new ServiceFiltroImpl();

      // Obtengo la lista de Ocs
      listaOcs = service.getListOrganosContratacionFromSql(filtroOcs);
      log.debug("[FiltroSqlLoader] - Obtenida ListaOcs. Tamaño: {}", listaOcs.size());

      // Obtengo la lista de Provincias
      String sqlProvincias = "SELECT provincia, codigo_ine, codigo_nuts FROM Provincias";
      List<Provincia> listaProvincias = service.getListProvinciasFromSql(sqlProvincias);
      log.debug("[FiltroSqlLoader] - Obtenida  ListaProvincias. Tamaño: {}", listaProvincias.size());

      // Convierto la lista de Provincias en un Map para mejorar la eficiencia de las búsquedas
      Map<String, String> ineToNutsMap = listaProvincias.stream()
          .collect(Collectors.toMap(
              Provincia::getCodigo_ine,
              Provincia::getCodigo_nuts
          ));

      // Recorro la lista de OCs para añadirle el valor NUTS a cada OC
      listaOcs.forEach(oc -> {
        if (oc.getCodigo_postal() != null && oc.getCodigo_postal().length() >= 2) {
          String codigoINE = oc.getCodigo_postal().substring(0, 2);
          String nuts = ineToNutsMap.get(codigoINE);
          if (nuts != null) {
            oc.setCodigo_nuts(nuts);
          }
        }
        log.info(oc.toString());
      });

      // Convierto la listOCs a un Map<IdPlataforma, Nuts>
      Map<String, String> mapIdPlataformaNuts = listaOcs.stream()
          .collect(Collectors.toMap(
              OrganoContratacion::getId_plataforma,
              OrganoContratacion::getCodigo_nuts
          ));

      VariablesGlobales.setMapFiltroSql(mapIdPlataformaNuts);
      log.debug("[FiltroSqlLoader] - Mapa cargado en VariablesGlobales. Tamaño: {}", mapIdPlataformaNuts.size());
      mapIdPlataformaNuts.forEach((key, value) -> log.debug("IdPlataforma: {}, Nuts: {}", key, value));

    } else {
      log.debug("[FiltroSqlLoader] - Filtro SQL vacío o inválido. Se omite carga.");
      VariablesGlobales.setFiltroOcs("");
      VariablesGlobales.setMapFiltroSql(new HashMap<>());
    }
  }
}
