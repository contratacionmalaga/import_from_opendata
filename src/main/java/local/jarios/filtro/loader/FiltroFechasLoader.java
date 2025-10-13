package local.jarios.filtro.loader;

import local.jarios.common.util.Constantes;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.interfaces.FiltroLoader;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Description: Author: juan Date: 13/10/2025 Team:
 */
@Slf4j
public class FiltroFechasLoader implements FiltroLoader {

  private final PropertiesManagerService propertyManager = PropertiesManagerServiceImpl.getInstance();

  @Override
  public void cargar() throws PropertiesManagerException {

    // Obtención de las fechas
    String filtroFechaInicialStr = propertyManager.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_FECHAINICIALLECTURA);
    String filtroFechaFinalStr = propertyManager.getProperty(PropertiesFiles.FILTER, PropertiesKeys.FILTER_FECHAFINALLECTURA);

    LocalDateTime fechaInicial;
    LocalDateTime fechaFinal;

    boolean inicioValido = filtroFechaInicialStr != null && !filtroFechaInicialStr.isBlank();
    boolean finValido = filtroFechaFinalStr != null && !filtroFechaFinalStr.isBlank();

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    if (inicioValido && finValido) {
      // Ambas fechas debugrmadas
      fechaInicial = LocalDate.parse(filtroFechaInicialStr, formatter).atStartOfDay();
      fechaFinal = LocalDate.parse(filtroFechaFinalStr, formatter).atTime(23, 59, 59);
      log.debug("[FiltroFechasLoader] - Ambas fechas debugrmadas correctamente.");

    } else if (inicioValido) {
      // Solo inicio debugrmada
      fechaInicial = LocalDate.parse(filtroFechaInicialStr, formatter).atStartOfDay();
      fechaFinal = getFechaFinalPorDefecto();
      log.debug("[FiltroFechasLoader] - Solo fecha inicial debugrmada. Fecha final por defecto usada: {}", fechaFinal);

    } else if (finValido) {
      // Solo fin debugrmada
      fechaInicial = getFechaInicialPorDefecto();
      fechaFinal = LocalDate.parse(filtroFechaFinalStr, formatter).atTime(23, 59, 59);
      log.debug("[FiltroFechasLoader] - Solo fecha final debugrmada. Fecha inicial por defecto usada: {}", fechaInicial);

    } else {
      // Ninguna debugrmada
      fechaInicial = getFechaInicialPorDefecto();
      fechaFinal = getFechaFinalPorDefecto();
      log.debug("[FiltroFechasLoader] - Fechas no debugrmadas. Se aplican valores por defecto.");
    }

    VariablesGlobales.setFiltroFechaInicial(fechaInicial);
    VariablesGlobales.setFiltroFechaFinal(fechaFinal);

    log.debug("[FiltroFechasLoader] - Fechas establecidas. Inicio: {}, Fin: {}", fechaInicial, fechaFinal);
  }

  private LocalDateTime getFechaInicialPorDefecto() {
    return LocalDate.now().atTime(0, 0);
  }

  private LocalDateTime getFechaFinalPorDefecto() {
    return LocalDate.parse(Constantes.FECHA_FINAL_LECTURA, DateTimeFormatter.ISO_LOCAL_DATE).atTime(23, 59, 59);
  }
}
