package local.jarios.helpers;

import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Clase utilitaria para operaciones relacionadas con tiempo y fechas.
 * <p>
 * Proporciona métodos para obtener la fecha y hora actual en zonas horarias específicas.
 * </p>
 * <p>
 * Author: juan Date: 21/06/2025
 */
@Slf4j
public final class LocalDateTimeHelper {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd'T'HH:mm:ss");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


  /**
   * Constructor privado para evitar instanciación.
   */
  private LocalDateTimeHelper() {
  }

  /**
   * Obtiene la fecha y hora local actual en la zona horaria de Madrid (Europe/Madrid).
   *
   * @return {@link LocalDateTime} con la fecha y hora actual en Madrid.
   */
  public static LocalDateTime getLocalDateTimeNow() {
    ZoneId zonaMadrid = ZoneId.of("Europe/Madrid");
    return LocalDateTime.now(zonaMadrid);
  }

  /**
   * Calcula la diferencia entre dos instantes {@link LocalDateTime} y devuelve una cadena con la
   * duración en formato "Xh Ym Zs Wms".
   *
   * @param localDateTimeInicial Fecha y hora inicial.
   * @param localDateTimeFinal   Fecha y hora final.
   * @return Cadena formateada con la duración entre las dos fechas.
   */
  public static String getDiferenciaLocalDateTime(
      LocalDateTime localDateTimeInicial, LocalDateTime localDateTimeFinal) {

    //
    Duration duracion = Duration.between(localDateTimeInicial, localDateTimeFinal);
    long horas = duracion.toHours();
    long minutos = duracion.toMinutesPart();
    long segundos = duracion.toSecondsPart();
    long milisegundos = duracion.toMillisPart();
    String msg = String.format("%dh %dm %ds %dms", horas, minutos, segundos, milisegundos);
    log.debug("[getDiferenciaLocalDateTime] {}", msg);
    return msg;
  }

  public static String getFechaHoraFormateada(LocalDateTime fechaHora) {

    LocalDateTime fecha = (fechaHora != null) ? fechaHora : LocalDateTime.now();
    return fecha.format(DATE_TIME_FORMATTER);
  }

  public static Optional<LocalDate> parseFechaSiValida(String fechaStr) {

    if (fechaStr == null || fechaStr.isBlank()) {
      log.debug("[parseFechaSiValida] - El parámetro es NULL o vacío.");
      return Optional.empty();
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constantes.FORMATO_FECHA);
    log.debug("[parseFechaSiValida] - Usando formato: {}", Constantes.FORMATO_FECHA);

    try {
      LocalDate fecha = LocalDate.parse(fechaStr, formatter);
      log.debug("[parseFechaSiValida] - La fecha es válida: {}", fecha);
      return Optional.of(fecha);
    } catch (DateTimeParseException ex) {
      log.debug("[parseFechaSiValida] - La fecha NO es válida: {}. Detalle: {}", fechaStr,
                ex.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Obtiene la fecha y hora local actual en la zona horaria de Madrid (Europe/Madrid).
   *
   * @return {@link LocalDateTime} con la fecha y hora actual en Madrid.
   */
  public static LocalDateTime getLocalDateTimeFromXmlGregorianCalendar(
      XMLGregorianCalendar xmlGregorianCalendar
  ) {
    return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
  }
}
