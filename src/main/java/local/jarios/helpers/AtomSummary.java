package local.jarios.helpers;

import java.util.Locale;
import java.util.regex.Pattern;

/** Valores textuales de las etiquetas del summary de PLACSP. */
public record AtomSummary(String expediente, String organoContratacion, String importe, String estado) {

  private static final String LABEL =
      "id\\s+(?:licitaci[oó]n|encargo|consulta)|[oó]rgano\\s+de\\s+contrataci[oó]n|importe|estado";
  private static final Pattern FIELD =
      Pattern.compile("(?isU)(?:^|;)\\s*(" + LABEL + ")\\s*:(.*?)(?=;\\s*(?:" + LABEL + ")\\s*:|\\z)");

  public static AtomSummary parse(String summary) {
    String expediente = null;
    String organo = null;
    String importe = null;
    String estado = null;
    var fields = FIELD.matcher(summary == null ? "" : summary);
    while (fields.find()) {
      String label = fields.group(1).toLowerCase(Locale.ROOT);
      String value = fields.group(2).strip();
      if (value.isEmpty()) {
        continue;
      }
      if (label.startsWith("id")) {
        expediente = value;
      } else if (label.equals("importe")) {
        importe = value;
      } else if (label.equals("estado")) {
        estado = value;
      } else {
        organo = value;
      }
    }
    return new AtomSummary(expediente, organo, importe, estado);
  }
}
