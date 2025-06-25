package local.jarios.helpers;

import local.jarios.common.util.Constantes;
import local.jarios.enums.TipoFecha;
import local.jarios.exceptions.MiInvalidDateFormatException;
import lombok.extern.slf4j.Slf4j;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
public final class DateTimeHelper {

    private static final Pattern FECHA_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Constructor privado para evitar instanciación.
     */
    private DateTimeHelper() { }

    /**
     * Obtiene la fecha y hora local actual en la zona horaria de Madrid (Europe/Madrid).
     *
     * @return {@link LocalDateTime} con la fecha y hora actual en Madrid.
     */
    public static LocalDateTime getLocalDateTimeNow() {
        ZoneId zonaMadrid = ZoneId.of("Europe/Madrid");
        return LocalDateTime.now(zonaMadrid);
    }

    public static String getDateTime(String date, String time) {
        String dateTime;
        if ((date == null) && (time == null)) {
            dateTime = null;
        } else {
            dateTime = date + " " + time;
        }
        return dateTime;
    }

    /**
     * Devuelve un valor java.sql.Date a partir de un un XMLGregorianCalendar
     *
     * @param   xmlGregorianCalendar El valor que queremos convertir en java.sql.Date
     *
     * @return  java.sql.Date
     */
    public static Date getDateFromXMLGregorianCalendar(XMLGregorianCalendar xmlGregorianCalendar) {

        //Convertir XMLGregorianCalendar a java.util.Date
        var gregorianCalendar = xmlGregorianCalendar.toGregorianCalendar();
        var utilDate = gregorianCalendar.getTime();

        //Convertir java.util.Date a java.sql.Date
        return new Date(utilDate.getTime());
    }

    /**
     * Devuelve un valor java.sql.Time a partir de un un XMLGregorianCalendar
     *
     * @param   xmlGregorianCalendar El valor que queremos convertir en java.sql.Time
     *
     * @return  java.sql.Time
     */
    public static Time getTimeFromXMLGregorianCalendar(XMLGregorianCalendar xmlGregorianCalendar) {

        //Convertir XMLGregorianCalendar a java.util.Date
        var gregorianCalendar = xmlGregorianCalendar.toGregorianCalendar();
        var utilDate = gregorianCalendar.getTime();

        //Convertir java.util.Date a java.sql.Time
        return new Time(utilDate.getTime());
    }

    public static LocalDateTime getLocalDateTime(Date date, Time time) {

        // Convertir Date a LocalDate
        LocalDate localDate = date.toLocalDate();

        // Comprobar si el parámetro Time es null
        LocalTime localTime;

        //C onvertir Time a LocalTime (si estás usando java.sql.Time)
        if (time != null) {
            // Convertir Time a LocalTime si no es null
            localTime = time.toLocalTime();
        } else {
            // Asignar un valor predeterminado (por ejemplo, medianoche) si Time es null
            localTime = LocalTime.MIDNIGHT;
        }

        //Combinar LocalDate y LocalTime en LocalDateTime
        return LocalDateTime.of(localDate, localTime);
    }

    public static boolean esDateCorrecta (Date fecha) {

        //
        var fechaLocal = fecha.toString();

        //
        if (FECHA_PATTERN.matcher(fechaLocal).matches()) {

            //
            var annio = fecha.toString().substring(0, 4);

            //
            return (annio.compareTo(Constantes.ANNIO_INICIAL) > 0) &&
                    (annio.compareTo(Constantes.ANNIO_FINAL) < 0);
        }

        //
        return false;
    }

    /**
     * Devuelve un LocalDate a partir de un String.
     * Utilizado en la lectura de los intervalos de fechas para la importacion en el fichero properties
     *
     * @param fecha Fecha leída desde el fichero properties
     * @return LocalDate Transformación de la fecha desde String a LocalDate
     */
    public static LocalDateTime parsearFecha(
            String fecha,
            String fechaPorDefecto,
            TipoFecha tipoFecha) throws MiInvalidDateFormatException {

        // Declaro la variable que contendrá el valor que vamos a devolver
        if (fecha == null || fecha.isEmpty()) {

            //
            fecha = fechaPorDefecto;
        }

        if (fechaPorDefecto.isEmpty()) {

            // Devuelvo la fecha y hora actual
            return LocalDateTime.now();
        }

        // Validar que el formato de la fecha sea correcto (aaaa-MM-dd)
        if (!FECHA_PATTERN.matcher(fecha).matches()) {

            throw new MiInvalidDateFormatException("La fecha '" + fecha + "' no tiene el formato esperado 'yyyy-MM-dd'.");
        }

        //
        if (tipoFecha == TipoFecha.INICIAL) {
            fecha = fecha + "T23:59:59";
        } else {
            fecha = fecha + "T00:00:01";
        }

        //
        try {

            // Intentamos parsear la fecha con el formato esperado
            return LocalDateTime.parse(fecha, DATE_TIME_FORMATTER);

        } catch (DateTimeParseException ex) {

            //
            var errorMessage = String.format(
                    "%sError al parsear la fecha '%s' con el formato esperado '%s'. Detalle del error: %s",
                    Constantes.TABULADOR_2,
                    DATE_TIME_FORMATTER,
                    fecha, ex.getMessage());
            //
            log.error(errorMessage);

            //
            throw new MiInvalidDateFormatException(errorMessage, ex);
        }
    }

    public static boolean esFechaValida(
            String fechaInicial,
            String fechaFinal,
            Timestamp updated) throws MiInvalidDateFormatException {

        // Si la fecha inicial está vacía, asignamos la fecha actual
        LocalDateTime fechaInicialParsed = parsearFecha(fechaInicial, LocalDateTime.now().toString(), TipoFecha.INICIAL);

        // Si la fecha final está vacía, asignamos el 01-01-2018
        LocalDateTime fechaFinalParsed = parsearFecha(fechaFinal, "2018-01-01T00:00:00", TipoFecha.FINAL);

        // Convertimos el timestamp "updated" a LocalDateTime para poder compararlo
        var updatedParsed = updated.toLocalDateTime();

        // Comparamos si la fecha "updated" está entre la fecha inicial y la fecha final
        return updatedParsed.isBefore(fechaInicialParsed) && updatedParsed.isAfter(fechaFinalParsed);
    }

    public static boolean esFechaInvalida(String fecha) {

        if (fecha.isBlank()) {
            return true;
        }

        if (fecha.)
    }

    public static LocalDate getLocalDateFromString(String fecha) {

        return true;
    }
}
