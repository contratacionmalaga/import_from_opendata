package local.jarios.helpers;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.LoggerFactory;

public final class LogbackHelper {

  private LogbackHelper() {}

  public static Optional<File> getCurrentLogFile() {
    var iLoggerFactory = LoggerFactory.getILoggerFactory();
    if (!(iLoggerFactory instanceof LoggerContext ctx)) {
      return Optional.empty();
    }

    // "FILE" es el nombre del appender en tu logback.xml
    var logger =
        ctx.getLogger("local.jarios"); // o ctx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

    Optional<File> loggerFile = getAppenderFile(logger.getAppender("FILE"));
    if (loggerFile.isPresent()) {
      return loggerFile;
    }

    // por si no está colgado del logger concreto, intentamos en root
    var root = ctx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    return getAppenderFile(root.getAppender("FILE"));
  }

  private static Optional<File> getAppenderFile(Object appender) {
    if (!(appender instanceof FileAppender<?> fileAppender)) {
      return Optional.empty();
    }

    String file = fileAppender.getFile();
    if (file == null || file.isBlank()) {
      return Optional.empty();
    }

    try {
      Path realPath = Path.of(file).toAbsolutePath().normalize().toRealPath();
      return Files.isRegularFile(realPath) ? Optional.of(realPath.toFile()) : Optional.empty();
    } catch (InvalidPathException | IOException ex) {
      return Optional.empty();
    }
  }
}
