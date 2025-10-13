package local.jarios.filtro.loader;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.interfaces.FiltroLoader;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
@Slf4j
public class FiltroNutsLoader implements FiltroLoader {

  private final PropertiesManagerService propertyManager = PropertiesManagerServiceImpl.getInstance();

  @Override
  public void cargar() throws PropertiesManagerException {
    String rawNutsFilter = propertyManager.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_NUTS);
    log.debug("[FiltroNutsLoader] - Valor leído del properties: '{}'", rawNutsFilter);

    Set<String> codigoNuts = parsearFiltroNuts(rawNutsFilter);

    VariablesGlobales.setFiltroNuts(new HashSet<>(codigoNuts));
    log.debug("[FiltroNutsLoader] - Filtro NUTS cargado con {} elemento(s): {}", codigoNuts.size(), codigoNuts);
  }

  private Set<String> parsearFiltroNuts(String filtro) {
    if (filtro == null || filtro.isBlank()) {
      return Set.of(); // retorna conjunto inmutable vacío
    }

    return Arrays.stream(filtro.split(","))
        .map(String::trim)
        .filter(codigo -> !codigo.isEmpty())
        .collect(Collectors.toSet());
  }
}
