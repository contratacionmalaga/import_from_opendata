package local.jarios.helpers;

import local.jarios.common.util.Constantes;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Clase utilitaria para operaciones relacionadas con tiempo y fechas.
 * <p>
 * Proporciona métodos para obtener la fecha y hora actual en zonas horarias específicas.
 * </p>
 *
 * Author: juan
 * Date: 21/06/2025
 */
@Slf4j
public final class LocalDateTimeHelper {

    private static final Pattern FECHA_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    /**
     * Constructor privado para evitar instanciación.
     */
    private LocalDateTimeHelper() { }

    /**
     * Obtiene la fecha y hora local actual en la zona horaria de Madrid (Europe/Madrid).
     *
     * @return {@link LocalDateTime} con la fecha y hora actual en Madrid.
     */
    public static LocalDateTime getLocalDateTimeNow() {
        ZoneId zonaMadrid = ZoneId.of("Europe/Madrid");
        return LocalDateTime.now(zonaMadrid);
    }

    public static LocalDate parseLocalDate(String fechaStr) throws DateTimeParseException {
        if (fechaStr == null || fechaStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(fechaStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error("[parseLocalDate] - Error al parser la fecha: {}", fechaStr);
            return null;
        }
    }

    /**
     * Calcula la diferencia entre dos instantes {@link LocalDateTime} y devuelve
     * una cadena con la duración en formato "Xh Ym Zs Wms".
     *
     * @param localDateTimeInicial Fecha y hora inicial.
     * @param localDateTimeFinal Fecha y hora final.
     * @return Cadena formateada con la duración entre las dos fechas.
     */
    public static String getDiferenciaLocalDateTime(LocalDateTime localDateTimeInicial, LocalDateTime localDateTimeFinal) {
        Duration duracion = Duration.between(localDateTimeInicial, localDateTimeFinal);
        long horas = duracion.toHours();
        long minutos = duracion.toMinutesPart();
        long segundos = duracion.toSecondsPart();
        long milisegundos = duracion.toMillisPart();
        return String.format("%dh %dm %ds %dms", horas, minutos, segundos, milisegundos);
    }

    /**
     *
     * @return Valor con la fecha donde finaliza la importación de los datos
     */
    private static LocalDateTime getFechaFromProperty(String campo, ) {

        PropertiesManagerService propertiesManagerService = PropertiesManagerServiceImpl.getInstance();

        //
        var fechaFromProperty = propertiesManagerService.getProperty(Constantes.FILTER_PROPERTIES, campo);

        //
        LocalDate localDate = LocalDateTimeHelper.parseLocalDate(fechaFromProperty);
    }

    public static String getFechaHoraFormateada(LocalDateTime fechaHora) {

        LocalDateTime fecha = (fechaHora != null) ? fechaHora.toLocalDateTime() : LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return fecha.format(formatter);
    }
}
