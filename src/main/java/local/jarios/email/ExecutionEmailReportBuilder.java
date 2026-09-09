package local.jarios.email;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Log;
import org.apache.commons.text.StringEscapeUtils;

/** Construye el asunto y el cuerpo HTML del reporte de ejecucion OpenData. */
public final class ExecutionEmailReportBuilder {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DecimalFormat INTEGER_FORMAT =
      new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.of("es", "ES")));

  private ExecutionEmailReportBuilder() {}

  public static String buildSuccessSubject(
      String processType, LugarImportacion lugarImportacion, Estadistica estadistica) {
    return "Importación OpenData finalizada correctamente"
        + " · "
        + normalize(processType)
        + " · "
        + value(lugarImportacion)
        + " · "
        + formatNumber(countUsefulChanges(estadistica))
        + " cambios";
  }

  public static String buildErrorSubject(
      String processType, String importOrigin, Throwable exception, String tipoError) {
    String errorType =
        exception == null ? cleanErrorType(tipoError) : exception.getClass().getSimpleName();
    return "ERROR · Importación OpenData "
        + normalize(processType)
        + " interrumpida · "
        + normalize(importOrigin)
        + " · "
        + normalize(errorType);
  }

  public static String buildSuccessBody(
      String appName,
      String appVersion,
      String processType,
      OpenDataExecutionContext context,
      Estadistica estadistica) {
    Log log = estadistica == null ? null : estadistica.getMiLog();
    LugarImportacion lugarImportacion = resolveLugarImportacion(context, log);
    TipoSindicacion tipoSindicacion = resolveTipoSindicacion(context, log);
    long historicos = safeLong(estadistica == null ? null : estadistica.getTotalHistoricos());
    long insertar =
        safeLong(estadistica == null ? null : estadistica.getNumRegistrosHistoricosInsertar());
    long actualizar =
        safeLong(estadistica == null ? null : estadistica.getNumRegistrosHistoricosActualizar());
    long rechazar =
        safeLong(estadistica == null ? null : estadistica.getNumRegistrosHistoricosRechazar());
    long cambiosUtiles = insertar + actualizar;
    long deletedEntries = safeLong(estadistica == null ? null : estadistica.getNumDeletedEntries());
    long ficherosAtoms = safeLong(estadistica == null ? null : estadistica.getNumFicherosAtoms());
    long entries = safeLong(estadistica == null ? null : estadistica.getNumEntries());
    String duracionParseo = value(estadistica == null ? null : estadistica.getDuracionParseo());
    String duracionPersistencia =
        value(estadistica == null ? null : estadistica.getDuracionPersistencia());
    String duracionTotal =
        formatDuration(
            parseDurationMillis(duracionParseo) + parseDurationMillis(duracionPersistencia));

    StringBuilder html = new StringBuilder(12_000);
    appendHeader(html);
    html.append("<div class=\"hero\"><table class=\"hero-table\"><tr><td class=\"hero-main\">");
    html.append("<h1>Ejecución completada correctamente</h1>");
    html.append("<p>La importación <strong>")
        .append(escape(processType))
        .append("</strong> desde <strong>")
        .append(escape(value(lugarImportacion)))
        .append("</strong> ha terminado sin errores. Se han procesado ")
        .append(formatNumber(ficherosAtoms))
        .append(" ficheros ATOM y ")
        .append(formatNumber(historicos))
        .append(" históricos; ")
        .append(formatNumber(cambiosUtiles))
        .append(" registros requieren acción real de persistencia.</p>");
    html.append("</td><td class=\"runtime\">");
    appendRuntimeRow(html, "Aplicacion", value(appName) + " " + value(appVersion));
    appendRuntimeRow(html, "Proceso", processType);
    appendRuntimeRow(html, "Origen", value(lugarImportacion));
    appendRuntimeRow(html, "Sindicacion", value(tipoSindicacion));
    appendRuntimeRow(html, "Equipo", estadistica == null ? null : estadistica.getEquipo());
    html.append("</td></tr></table></div>");

    html.append("<table class=\"summary\"><tr>");
    appendMetric(html, "Históricos", formatNumber(historicos), "total_historicos");
    appendMetric(
        html,
        "Cambios útiles",
        formatNumber(cambiosUtiles),
        formatNumber(insertar) + " nuevos · " + formatNumber(actualizar) + " actualizados");
    appendMetric(html, "Rechazados", formatNumber(rechazar), percentage(rechazar, historicos));
    appendMetric(html, "Duración total", duracionTotal, "parseo + persistencia");
    html.append("</tr></table>");

    html.append("<table class=\"grid\"><tr>");
    html.append("<td class=\"section\"><h2>Resultado de persistencia</h2>");
    appendBar(html, "Insertar", insertar, Math.max(cambiosUtiles, 1), "good");
    appendBar(html, "Actualizar", actualizar, Math.max(cambiosUtiles, 1), "info");
    appendBar(html, "Rechazar", rechazar, Math.max(historicos, 1), "warn");
    appendBar(
        html, "Deleted entries", deletedEntries, Math.max(entries + deletedEntries, 1), "danger");
    html.append("</td>");

    html.append("<td class=\"section\"><h2>Rendimiento</h2><table class=\"kv\"><tbody>");
    appendTableRow(html, "Duración parseo", duracionParseo);
    appendTableRow(html, "Duración BD", duracionPersistencia);
    appendTableRow(html, "Cambios útiles", percentage(cambiosUtiles, historicos));
    appendTableRow(html, "Ficheros ATOM", formatNumber(ficherosAtoms));
    appendTableRow(html, "Entries activas", formatNumber(entries));
    appendTableRow(html, "Deleted entries", formatNumber(deletedEntries));
    html.append("</tbody></table></td></tr><tr>");

    html.append("<td class=\"section\"><h2>Configuración aplicada</h2><table class=\"kv\"><tbody>");
    appendTableRow(
        html,
        "Inicio lectura",
        formatDateTime(context == null ? null : context.getFiltroFechaInicial()));
    appendTableRow(
        html,
        "Fin lectura",
        formatDateTime(context == null ? null : context.getFiltroFechaFinal()));
    appendTableRow(
        html,
        "NIFs filtro",
        formatNumber(safeLong(estadistica == null ? null : estadistica.getNumNifsFiltro())));
    appendTableRow(
        html,
        "Órganos filtro",
        formatNumber(
            safeLong(estadistica == null ? null : estadistica.getNumOrganosContratacionFiltro())));
    appendTableRow(
        html,
        "Códigos postales",
        abbreviate(context == null ? null : context.getFiltroCodigosPostales(), 80));
    appendTableRow(
        html, "Fecha generacion", formatSqlDate(log == null ? null : log.getFechaGeneracion()));
    html.append("</tbody></table></td>");

    html.append("<td class=\"section\"><h2>Datos técnicos</h2><table class=\"kv\"><tbody>");
    appendTableRow(html, "Log creado", formatDateTime(log == null ? null : log.getCreatedAt()));
    appendTableRow(
        html,
        "Estadistica creada",
        formatDateTime(estadistica == null ? null : estadistica.getCreatedAt()));
    appendTableRow(html, "Registros log", formatNumber(log == null ? 0L : log.getNRegistros()));
    appendTableRow(html, "Version", appVersion);
    html.append("</tbody></table></td></tr></table>");

    html.append("<div class=\"notice\"><strong>Lectura rápida:</strong> ejecución correcta de ")
        .append(escape(processType))
        .append(", volumen procesado: ")
        .append(formatNumber(historicos))
        .append(" históricos, cambios útiles: ")
        .append(formatNumber(cambiosUtiles))
        .append(".</div>");
    html.append("<div class=\"footer\">Generado automáticamente por import-from-opendata · ")
        .append(escape(processType))
        .append(".</div>");
    html.append("</div></body></html>");
    return html.toString();
  }

  private static void appendHeader(StringBuilder html) {
    html.append("<!doctype html><html><head><meta charset=\"UTF-8\"><style>");
    html.append(
        "body{margin:0;background:#f4f7fb;color:#1f2937;font-family:Arial,Helvetica,sans-serif;}"
            + ".wrap{max-width:760px;margin:0 auto;padding:22px;}"
            + ".hero,.metric,.section,.notice{background:#fff;border:1px solid #d8e0ea;border-radius:8px;}"
            + ".hero{padding:20px}.hero-table{width:100%;border-collapse:collapse}.hero-main{padding-right:18px;vertical-align:top}.runtime{vertical-align:top;width:250px}"
            + "h1{font-size:20px;font-weight:600;line-height:1.25;margin:0 0 8px}h2{font-size:14px;font-weight:600;margin:0 0 10px}"
            + "p{font-size:13px;line-height:1.5;margin:0;color:#667085}.summary{width:100%;border-spacing:10px;margin:4px -10px}.metric{padding:12px;vertical-align:top;width:25%}"
            + ".label{font-size:12px;color:#667085;margin-bottom:4px}.value{font-size:22px;font-weight:600;line-height:1.1}.note{font-size:12px;color:#667085;margin-top:6px}"
            + ".grid{width:100%;border-spacing:14px;margin:0 -14px}.section{padding:14px;vertical-align:top;width:50%}.kv{width:100%;border-collapse:collapse;font-size:12px}"
            + ".kv th,.kv td{padding:8px 0;border-bottom:1px solid #d8e0ea;text-align:left;vertical-align:top}.kv th{color:#667085;font-weight:600}.kv td:last-child,.kv th:last-child{text-align:right;font-variant-numeric:tabular-nums}"
            + ".bar-row{font-size:12px;margin:10px 0}.track{height:8px;border-radius:99px;background:#edf2f7;overflow:hidden}.fill{height:8px;border-radius:99px}.good{background:#168a55}.info{background:#2563a8}.warn{background:#b25e09}.danger{background:#b42318}"
            + ".notice{padding:14px;font-size:13px;line-height:1.45;margin-top:14px}.footer{font-size:12px;color:#667085;text-align:center;margin-top:12px}"
            + "@media(max-width:640px){.metric,.section,.hero-main{display:block;width:auto;padding-right:0}.runtime{display:block;width:auto}.summary,.grid{border-spacing:0}.metric,.section{display:block;margin:0 0 10px}}");
    html.append("</style></head><body><div class=\"wrap\">");
  }

  private static LugarImportacion resolveLugarImportacion(
      OpenDataExecutionContext context, Log log) {
    if (context != null && context.getLugarImportacion() != null) {
      return context.getLugarImportacion();
    }
    return log == null ? null : log.getLugarImportacion();
  }

  private static TipoSindicacion resolveTipoSindicacion(OpenDataExecutionContext context, Log log) {
    if (context != null && context.getTipoSindicacion() != null) {
      return context.getTipoSindicacion();
    }
    return log == null ? null : log.getTipoSindicacion();
  }

  private static void appendRuntimeRow(StringBuilder html, String label, String value) {
    html.append(
            "<div style=\"font-size:13px;border-bottom:1px solid #d8e0ea;padding:0 0 7px;margin:0 0 7px\"><span style=\"color:#667085\">")
        .append(escape(label))
        .append("</span><span style=\"float:right;text-align:right\">")
        .append(escape(value(value)))
        .append("</span><div style=\"clear:both\"></div></div>");
  }

  private static void appendMetric(StringBuilder html, String label, String value, String note) {
    html.append("<td class=\"metric\"><div class=\"label\">")
        .append(escape(label))
        .append("</div><div class=\"value\">")
        .append(escape(value))
        .append("</div><div class=\"note\">")
        .append(escape(note))
        .append("</div></td>");
  }

  private static void appendBar(
      StringBuilder html, String label, long value, long maxValue, String colorClass) {
    long width = Math.min(100, Math.max(0, Math.round(value * 100.0 / maxValue)));
    html.append("<div class=\"bar-row\"><div style=\"margin-bottom:4px\"><span>")
        .append(escape(label))
        .append("</span><span style=\"float:right;font-variant-numeric:tabular-nums\">")
        .append(formatNumber(value))
        .append(
            "</span><div style=\"clear:both\"></div></div><div class=\"track\"><div class=\"fill ")
        .append(escape(colorClass))
        .append("\" style=\"width:")
        .append(width)
        .append("%\"></div></div></div>");
  }

  private static void appendTableRow(StringBuilder html, String label, String value) {
    html.append("<tr><th>")
        .append(escape(label))
        .append("</th><td>")
        .append(escape(value(value)))
        .append("</td></tr>");
  }

  private static long countUsefulChanges(Estadistica estadistica) {
    if (estadistica == null) {
      return 0L;
    }
    return safeLong(estadistica.getNumRegistrosHistoricosInsertar())
        + safeLong(estadistica.getNumRegistrosHistoricosActualizar());
  }

  private static long safeLong(Long value) {
    return value == null ? 0L : value;
  }

  private static String formatNumber(long value) {
    return INTEGER_FORMAT.format(value);
  }

  private static String percentage(long value, long total) {
    if (total <= 0L) {
      return "0%";
    }
    return String.format(Locale.of("es", "ES"), "%.1f%%", value * 100.0 / total);
  }

  private static String formatDateTime(LocalDateTime value) {
    return value == null ? "No informado" : value.format(DATE_TIME_FORMATTER);
  }

  private static String formatSqlDate(Date value) {
    return value == null ? "No informado" : value.toLocalDate().format(DATE_FORMATTER);
  }

  private static String abbreviate(String value, int max) {
    String normalized = value(value);
    if ("No informado".equals(normalized) || normalized.length() <= max) {
      return normalized;
    }
    return normalized.substring(0, Math.max(0, max - 3)) + "...";
  }

  private static String formatDuration(long millis) {
    if (millis <= 0L) {
      return "No informado";
    }
    long totalSeconds = millis / 1000;
    long hours = totalSeconds / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;
    if (hours > 0) {
      return String.format(Locale.ROOT, "%dh %dm %ds", hours, minutes, seconds);
    }
    return String.format(Locale.ROOT, "%dm %ds", minutes, seconds);
  }

  private static long parseDurationMillis(String value) {
    if (value == null || value.isBlank() || "No informado".equals(value)) {
      return 0L;
    }
    long result = 0L;
    String[] parts = value.trim().split("\\s+");
    for (String part : parts) {
      if (part.endsWith("ms")) {
        result += parseDurationPart(part, "ms");
      } else if (part.endsWith("h")) {
        result += parseDurationPart(part, "h") * 3_600_000L;
      } else if (part.endsWith("m")) {
        result += parseDurationPart(part, "m") * 60_000L;
      } else if (part.endsWith("s")) {
        result += parseDurationPart(part, "s") * 1000L;
      }
    }
    return result;
  }

  private static long parseDurationPart(String value, String suffix) {
    try {
      return Long.parseLong(value.substring(0, value.length() - suffix.length()));
    } catch (NumberFormatException ex) {
      return 0L;
    }
  }

  private static String cleanErrorType(String tipoError) {
    if (tipoError == null || tipoError.isBlank()) {
      return "Error";
    }
    return tipoError.replace("[", "").replace("]", "");
  }

  private static String value(Object value) {
    return value == null || value.toString().isBlank() ? "No informado" : value.toString();
  }

  private static String normalize(String value) {
    return value == null || value.isBlank() ? "desconocido" : value;
  }

  private static String escape(String value) {
    return StringEscapeUtils.escapeHtml4(value(value));
  }
}
