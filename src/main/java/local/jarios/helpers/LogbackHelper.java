package local.jarios.helpers;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

public final class LogbackHelper {

  private LogbackHelper() {
  }

  public static Optional<File> getCurrentLogFile() {
    var iLoggerFactory = LoggerFactory.getILoggerFactory();
    if (!(iLoggerFactory instanceof LoggerContext ctx)) {
      return Optional.empty();
    }

    // "FILE" es el nombre del appender en tu logback.xml
    var logger = ctx.getLogger(
        "local.jarios"); // o ctx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

    var appender = logger.getAppender("FILE");
    if (appender instanceof FileAppender<?> fileAppender) {
      String file = fileAppender.getFile();
      if (file != null && !file.isBlank()) {
        File f = new File(file);
        return (f.exists() && f.isFile()) ? Optional.of(f) : Optional.empty();
      }
    }

    // por si no está colgado del logger concreto, intentamos en root
    var root = ctx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    var rootAppender = root.getAppender("FILE");
    if (rootAppender instanceof FileAppender<?> fileAppender) {
      String file = fileAppender.getFile();
      if (file != null && !file.isBlank()) {
        File f = new File(file);
        return (f.exists() && f.isFile()) ? Optional.of(f) : Optional.empty();
      }
    }

    return Optional.empty();
  }
}
