package local.jarios.filtro.loader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.filtro.interfaces.FiltroLoader;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** Description: Loader de filtros de fechas de lectura. Author: juan Date: 13/10/2025 */
@Slf4j
public class FiltroFechasLoader implements FiltroLoader {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  private final PropertiesManagerService propertyManager =
      PropertiesManagerServiceImpl.getInstance();

  @Getter private LocalDateTime fechaInicial;

  @Getter private LocalDateTime fechaFinal;

  @Override
  public void cargar(OpenDataExecutionContext context) throws PropertiesManagerException {

    String fechaInicialStr =
        propertyManager.getProperty(
            PropertiesFiles.FILTER, PropertiesKeys.FILTER_FECHAINICIALLECTURA);
    String fechaFinalStr =
        propertyManager.getProperty(
            PropertiesFiles.FILTER, PropertiesKeys.FILTER_FECHAFINALLECTURA);

    boolean inicioValido = esFechaValida(fechaInicialStr);
    boolean finValido = esFechaValida(fechaFinalStr);

    if (inicioValido && finValido) {

      fechaInicial = parseInicio(fechaInicialStr);
      fechaFinal = parseFin(fechaFinalStr);
      log.debug("Ambas fechas informadas correctamente.");

    } else if (inicioValido) {

      fechaInicial = parseInicio(fechaInicialStr);
      fechaFinal = getFechaFinalPorDefecto();
      log.debug("Solo fecha inicial informada. Fecha final por defecto usada: {}", fechaFinal);

    } else if (finValido) {

      fechaInicial = getFechaInicialPorDefecto();
      fechaFinal = parseFin(fechaFinalStr);
      log.debug("Solo fecha final informada. Fecha inicial por defecto usada: {}", fechaInicial);

    } else {

      fechaInicial = getFechaInicialPorDefecto();
      fechaFinal = getFechaFinalPorDefecto();
      log.debug("Fechas no informadas. Se aplican valores por defecto.");
    }

    validarRangoFechas();

    context.setFiltroFechaInicial(fechaInicial);
    context.setFiltroFechaFinal(fechaFinal);

    log.debug("Filtro de fechas establecido. Inicio: {}, Fin: {}", fechaInicial, fechaFinal);
  }

  // =========================================================
  // Métodos auxiliares
  // =========================================================

  private boolean esFechaValida(String fecha) {
    return fecha != null && !fecha.isBlank();
  }

  private LocalDateTime parseInicio(String fecha) {
    return LocalDate.parse(fecha, FORMATTER).atStartOfDay();
  }

  private LocalDateTime parseFin(String fecha) {
    return LocalDate.parse(fecha, FORMATTER).atTime(LocalTime.MAX);
  }

  private LocalDateTime getFechaInicialPorDefecto() {
    return LocalDate.now().atStartOfDay();
  }

  private LocalDateTime getFechaFinalPorDefecto() {
    return LocalDate.parse(Constantes.FECHA_FINAL_LECTURA, FORMATTER).atTime(LocalTime.MAX);
  }

  private void validarRangoFechas() {
    // Las fechas van desde ahora hacia el pasado, por lo tanto la inicial es posterior a la final
    if (fechaInicial.isBefore(fechaFinal)) {
      throw new IllegalArgumentException("La fecha inicial no puede ser ANTERIOR a la fecha final");
    }
  }
}
