package local.jarios.helpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

/** Mantiene la hora publicada y permite ordenar por el instante real. */
public final class AtomTimestamp {
  private static final ZoneId MADRID = ZoneId.of("Europe/Madrid");

  private AtomTimestamp() {}

  public static OffsetDateTime fromXml(XMLGregorianCalendar value) {
    int nanos = value.getFractionalSecond() == null ? 0
        : value.getFractionalSecond().movePointRight(9).intValue();
    LocalDateTime local = LocalDateTime.of(
        value.getYear(), value.getMonth(), value.getDay(), value.getHour(), value.getMinute(),
        value.getSecond(), nanos).truncatedTo(ChronoUnit.MICROS);
    if (value.getTimezone() == DatatypeConstants.FIELD_UNDEFINED) {
      return local.atZone(MADRID).toOffsetDateTime();
    }
    return local.atOffset(ZoneOffset.ofTotalSeconds(value.getTimezone() * 60));
  }

  public static Instant instant(LocalDateTime local, Integer offsetSeconds) {
    if (local == null) {
      return null;
    }
    return offsetSeconds == null
        ? local.atZone(MADRID).toInstant()
        : local.toInstant(ZoneOffset.ofTotalSeconds(offsetSeconds));
  }
}
