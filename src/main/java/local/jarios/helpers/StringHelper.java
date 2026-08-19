package local.jarios.helpers;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;

/** Interfaz para acciones sobre objetos String */
@Slf4j
public final class StringHelper {

  // Definimos el patrón como static final para evitar compilarlo repetidamente
  private static final Pattern PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

  // Patrón para validar lista de códigos postales (01-52) separados por ", "
  private static final String REGEX_FILTRO_CP =
      "^(0[1-9]|[1-4][0-9]|5[0-2])(, (0[1-9]|[1-4][0-9]|5[0-2]))*$";
  private static final Pattern PATTERN_FILTRO_CP = Pattern.compile(REGEX_FILTRO_CP);

  private static final String REGEX_FILTRO_NIF = "^[^,]{9}(, [^,]{9})*$";
  private static final Pattern PATTERN_FILTRO_NIF = Pattern.compile(REGEX_FILTRO_NIF);

  /** Constructor privado. */
  private StringHelper() {
    // NO IMPLEMENTADO
  }

  public static String eliminarCaracteres(String cadena) {

    if (cadena == null) {
      return null;
    }

    //  Decodifico la cadena
    String cadenaDecodificada = StringEscapeUtils.unescapeHtml4(cadena);

    String resultado =
        cadenaDecodificada.replace(Constantes.RETORNO_CARRO_HTML, Constantes.CADENA_VACIA);

    // Elimino los retornos de carro
    resultado = resultado.replace(Constantes.RETORNO_CARRO, Constantes.CADENA_VACIA);

    // Elimino los saltos de línea
    resultado = resultado.replace(Constantes.SALTO_LINEA, Constantes.CADENA_VACIA);

    return resultado;
  }

  /** Devuelve null si la cadena es null. */
  public static String trimToNull(String value) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  /**
   * Convierte el nombre de empresa a mayúsculas, limpia saltos de línea y reduce espacios
   * múltiples.
   */
  public static String sanitizePartyName(String value) {
    String sanitized = trimToNull(eliminarCaracteres(value));
    if (sanitized == null) {
      return null;
    }

    sanitized = sanitized.replaceAll("\\s+", " ");
    return sanitized.toUpperCase(Locale.ROOT);
  }

  /**
   * Sanitiza un NIF/CIF/NIE: - trim - quita espacios - quita guiones - quita puntos - convierte a
   * mayúsculas
   */
  public static String sanitizeNif(String value) {
    String sanitized = trimToNull(value);
    if (sanitized == null) {
      return null;
    }

    sanitized =
        sanitized.replace(" ", "").replace("-", "").replace(".", "").toUpperCase(Locale.ROOT);

    return sanitized.isEmpty() ? null : sanitized;
  }

  /** Variante opcional si quieres además quitar barras u otros separadores. */
  public static String sanitizeNifStrict(String value) {
    String sanitized = trimToNull(value);
    if (sanitized == null) {
      return null;
    }

    sanitized = sanitized.toUpperCase(Locale.ROOT).replaceAll("[\\s\\-\\.]", "");

    return sanitized.isEmpty() ? null : sanitized;
  }

  /** Opcional: quita acentos. */
  public static String removeAccents(String value) {
    if (value == null) {
      return null;
    }

    String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
    return normalized.replaceAll("\\p{M}", "");
  }

  public static boolean contieneCadena(String cadenaPrincipal, String cadenaBuscada) {

    // Normalizar las cadenas y convertir a minúsculas
    String cadenaPrincipalNormalizada = normalizarCadena(cadenaPrincipal);
    String cadenaBuscadaNormalizada = normalizarCadena(cadenaBuscada);

    // Verificar si la cadenaBuscada está contenida en la cadenaPrincipal
    return cadenaPrincipalNormalizada.contains(cadenaBuscadaNormalizada);
  }

  private static String normalizarCadena(String cadena) {

    if (cadena == null) {
      return "";
    }

    // Convertir la cadena a minúsculas y eliminar acentos
    String sinAcentos = Normalizer.normalize(cadena, Normalizer.Form.NFD);

    // Utilizamos Locale.ROOT para evitar variaciones de configuración regional
    return PATTERN.matcher(sinAcentos).replaceAll("").toLowerCase(Locale.ROOT);
  }

  public static String getNumeroConFormato(int numero) throws IllegalArgumentException {

    // Crear símbolos decimales personalizados
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setGroupingSeparator('.'); // separador de miles como punto
    symbols.setDecimalSeparator(','); // separador decimal como coma (opcional)

    // Crear formato con separador de miles y sin decimales
    DecimalFormat formato = new DecimalFormat("#,###", symbols);

    try {

      return formato.format(numero);

    } catch (IllegalArgumentException ex) {

      log.debug("[getNumeroConFormato] - Error: {}", ex.getMessage());
      throw ex;
    }
  }

  public static boolean isInvalidString(String cadena) {

    if (cadena == null) {
      log.debug("[isInvalidString] - La cadena es NULL. Cadena: {}", cadena);
      return true;
    }

    if (cadena.isBlank()) {
      log.debug("[isInvalidString] - La cadena es BLANK. Cadena: {}", cadena);
      return true;
    }

    log.debug("[isInvalidString] - La cadena no es NULL ni BLANK. Cadena: {}", cadena);
    return false;
  }

  public static boolean isValidString(String cadena) {

    if ((cadena != null) && (!cadena.isBlank())) {
      log.debug("[isValidString] - La cadena no es NULL ni BLANK. Cadena: {}", cadena);
      return true;
    }

    log.debug("[isValidString] - La cadena es NULL o BLANK. Cadena: {}", cadena);
    return false;
  }

  /**
   * Valida si una cadena cumple con el formato de lista de códigos postales (01-52) separados por
   * ", ". Ejemplo válido: "01, 02, 52"
   *
   * @param cadena Cadena a validar.
   * @return true si es válida, false en caso contrario.
   */
  public static boolean isValidFiltroCodigosPostales(String cadena) {
    if (isInvalidString(cadena)) {
      return false;
    }
    boolean esValido = PATTERN_FILTRO_CP.matcher(cadena).matches();
    log.debug("[isValidFiltroCodigosPostales] - Cadena: '{}', Es válido: {}", cadena, esValido);
    return esValido;
  }

  /**
   * Valida si una cadena cumple con el formato de lista nifs separadas por ", ". Ejemplo válido:
   * "P2900000G, P2900001H"
   *
   * @param cadena Cadena a validar.
   * @return true si es válida, false en caso contrario.
   */
  public static boolean isValidFiltroNifs(String cadena) {
    if (isInvalidString(cadena)) {
      return false;
    }
    boolean esValido = PATTERN_FILTRO_NIF.matcher(cadena).matches();
    log.debug("[isValidFiltroNifs] - Cadena: '{}', Es válido: {}", cadena, esValido);
    return esValido;
  }

  /**
   * Imprime un título formateado en el log, línea a línea, para que cada línea tenga su prefijo
   * completo.
   *
   * @param log Logger desde el que se imprime
   * @param titulo Texto del título
   */
  public static void generarTitulo(Logger log, String titulo) {

    if (titulo == null || titulo.isBlank()) {
      log.warn("Título nulo o vacío en logTitulo()");
      return;
    }

    int longitud = titulo.length();
    String separador = "=".repeat(longitud);

    List<String> lineas = List.of(separador, titulo, separador);

    lineas.forEach(linea -> log.info("{}", linea));
  }

  public static String limit(String value, int max) {
    String result = trimToNull(value);

    if (result == null) {
      return null;
    }

    if (max <= 0) {
      return "";
    }

    return result.length() <= max ? result : result.substring(0, max);
  }

  /**
   * Extrae el identificador situado tras el último '/' de una URL o identificador completo.
   *
   * @param id identificador completo
   * @return identificador corto
   */
  public static String obtenerIdCorto(String id) {
    if (id == null) {
      return null;
    }

    int idx = id.lastIndexOf('/');
    return idx >= 0 ? id.substring(idx + 1) : id;
  }

  public static String normalizeText(String value) {
    String sanitized = trimToNull(eliminarCaracteres(value));
    if (sanitized == null) {
      return null;
    }

    sanitized = sanitized.replaceAll("\\s+", " ");
    return sanitized;
  }

  public static String normalizeAndLimit(String value, int max) {
    String normalized = normalizeText(value);
    if (normalized == null) {
      return null;
    }

    return limit(normalized, max);
  }
}
