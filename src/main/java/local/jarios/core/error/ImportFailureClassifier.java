package local.jarios.core.error;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Locale;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.properties.exception.PropertiesManagerException;

/** Clasifica una causa técnica en una categoría estable para el operador. */
public final class ImportFailureClassifier {

  private ImportFailureClassifier() {}

  public static ImportErrorCategory classify(
      Throwable exception, OpenDataExecutionContext context) {
    for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
      ImportErrorCategory category = classifyByType(cause, context);
      if (category != null) {
        return category;
      }
    }
    return classifyByLegacyMessage(exception);
  }

  private static ImportErrorCategory classifyByType(
      Throwable cause, OpenDataExecutionContext context) {
    if (cause instanceof PropertiesManagerException) {
      return ImportErrorCategory.CONFIGURATION_INVALID;
    }
    if (cause instanceof MiUnknownHostException) {
      return ImportErrorCategory.DATABASE_HOST_UNRESOLVED;
    }
    if (cause instanceof SQLException sqlException) {
      String sqlState = sqlException.getSQLState();
      if (sqlState != null && sqlState.startsWith("28")) {
        return ImportErrorCategory.DATABASE_AUTHENTICATION;
      }
      if (sqlState != null && sqlState.startsWith("08")) {
        return ImportErrorCategory.DATABASE_UNAVAILABLE;
      }
    }
    if (cause instanceof UnknownHostException) {
      return isParsePhase(context)
          ? ImportErrorCategory.REMOTE_FEED_UNAVAILABLE
          : ImportErrorCategory.DATABASE_HOST_UNRESOLVED;
    }
    if (cause instanceof ConnectException || cause instanceof SocketTimeoutException) {
      return isParsePhase(context)
          ? ImportErrorCategory.REMOTE_FEED_UNAVAILABLE
          : ImportErrorCategory.DATABASE_UNAVAILABLE;
    }
    if (cause instanceof MiRepositoryException) {
      return ImportErrorCategory.PERSISTENCE_FAILED;
    }
    if (cause instanceof MiParseException) {
      return classifyByLegacyMessage(cause);
    }
    return null;
  }

  private static boolean isParsePhase(OpenDataExecutionContext context) {
    return context != null && "parse-open-data".equals(context.getCurrentPhase());
  }

  private static ImportErrorCategory classifyByLegacyMessage(Throwable exception) {
    String message = causeMessages(exception).toLowerCase(Locale.ROOT);
    if (message.contains("access denied")) return ImportErrorCategory.DATABASE_AUTHENTICATION;
    if (message.contains("ya existe una importacion en curso")
        || message.contains("exclusión de importación ya está adquirida")) {
      return ImportErrorCategory.IMPORT_ALREADY_RUNNING;
    }
    if (message.contains("base de datos vacia") || message.contains("entries existentes")) {
      return ImportErrorCategory.LOCAL_DATA_ALREADY_EXISTS;
    }
    if (message.contains("enlace next informado pero no válido")
        || message.contains("nextlink inicial no es válida")) {
      return ImportErrorCategory.FEED_NEXT_INVALID;
    }
    if (message.contains("ciclo detectado en la cadena de feeds")) return ImportErrorCategory.FEED_CYCLE;
    if (message.contains("jaxb") || message.contains("xml atom") || message.contains("unmarshaller")) {
      return ImportErrorCategory.ATOM_XML_INVALID;
    }
    if (message.contains("properties") || message.contains("configuración")) {
      return ImportErrorCategory.CONFIGURATION_INVALID;
    }
    if (message.contains("url") || message.contains("http") || message.contains("descarga")) {
      return ImportErrorCategory.REMOTE_FEED_UNAVAILABLE;
    }
    if (message.contains("unknown host") || message.contains("no such host")) {
      return ImportErrorCategory.DATABASE_HOST_UNRESOLVED;
    }
    if (message.contains("connection refused")
        || message.contains("connection timed out")
        || message.contains("connect timed out")) {
      return ImportErrorCategory.DATABASE_UNAVAILABLE;
    }
    if (message.contains("sessionfactory") || message.contains("hikaricp")) {
      return ImportErrorCategory.CONFIGURATION_INVALID;
    }
    return ImportErrorCategory.UNKNOWN;
  }

  private static String causeMessages(Throwable exception) {
    StringBuilder result = new StringBuilder();
    for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
      if (cause.getMessage() != null) result.append(' ').append(cause.getMessage());
    }
    return result.toString();
  }
}

