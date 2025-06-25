package local.jarios.helpers;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Date;
import java.sql.Time;

/**
 * @author Juan Antonio
 */
public final class GregorianCalendarHelper {

    private GregorianCalendarHelper() { }

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
}
