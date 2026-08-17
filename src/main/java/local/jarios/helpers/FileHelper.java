package local.jarios.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import local.jarios.common.util.Mensajes;
import lombok.extern.slf4j.Slf4j;

/** Interfaz para acciones sobre objetos File */
@Slf4j
public final class FileHelper {

  private FileHelper() {}

  /**
   * Analiza si un String que se pasa es un File válido (EXISTE, SE PUEDA LEER, .entity..)
   *
   * @param strPathFichero Fichero con la ruta absoluta
   * @return Devuelve un valor indicando si el fichero es valido y en caso contrario indica el
   *     motivo
   */
  public static boolean esFileValido(String strPathFichero) {
    return resolveReadableFile(strPathFichero) != null;
  }

  public static Path requireReadableFile(String strPathFichero) throws IOException {
    Path file = resolveReadableFile(strPathFichero);
    if (file == null) {
      throw new IOException("Fichero no valido: " + strPathFichero);
    }
    return file;
  }

  private static Path resolveReadableFile(String strPathFichero) {
    if (strPathFichero == null || strPathFichero.isBlank()) {
      log.warn(Mensajes.FILE_NOT_EXIST, strPathFichero);
      return null;
    }

    try {
      Path filePathFichero = Path.of(strPathFichero).toAbsolutePath().normalize();

      if (!Files.exists(filePathFichero)) {
        log.warn(Mensajes.FILE_NOT_EXIST, strPathFichero);
        return null;
      }

      Path realPath = filePathFichero.toRealPath();

      if (!Files.isRegularFile(realPath)) {
        log.warn(Mensajes.NOT_FILE, strPathFichero);
        return null;
      }

      if (!Files.isReadable(realPath)) {
        log.warn(Mensajes.FILE_NOT_READ, strPathFichero);
        return null;
      }

      return realPath;
    } catch (InvalidPathException | IOException ex) {
      log.warn(Mensajes.FILE_NOT_EXIST, strPathFichero);
      return null;
    }
  }
}
