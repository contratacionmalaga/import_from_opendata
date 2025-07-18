package local.jarios.helpers;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Interfaz para acciones sobre objetos GregorianCalendar
 */
public final class GregorianCalendarHelper {

    private GregorianCalendarHelper() { }

    /**
     * Devuelve un valor java.sql.Date a partir de un un XMLGregorianCalendar
     *
     * @param   xmlGregorianCalendar El valor que queremos convertir en java.sql.Date
     *
     * @return  java.time.LocalDate
     */
    public static LocalDate getDateFromXMLGregorianCalendar(XMLGregorianCalendar xmlGregorianCalendar) {

        if (xmlGregorianCalendar == null) {
            return null;
        }
        return xmlGregorianCalendar.toGregorianCalendar()
                .toZonedDateTime()
                .toLocalDate();
    }

    /**
     * Devuelve un valor java.sql.Time a partir de un un XMLGregorianCalendar
     *
     * @param   xmlGregorianCalendar El valor que queremos convertir en java.sql.Time
     *
     * @return  java.time.LocalTime
     */
    public static LocalTime getTimeFromXMLGregorianCalendar(XMLGregorianCalendar xmlGregorianCalendar) {

        if (xmlGregorianCalendar == null) {
            return null;
        }
        return xmlGregorianCalendar.toGregorianCalendar()
                .toZonedDateTime()
                .toLocalTime();
    }
}
