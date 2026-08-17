package local.jarios.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.FeedLocationHelper;
import local.jarios.helpers.FileHelper;
import local.jarios.interfaces.FeedSource;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación local de {@link FeedSource} que obtiene los feeds desde archivos en disco.
 *
 * <p>Utiliza un servicio centralizado de propiedades para resolver rutas y nombres de archivo,
 * asegurando que las rutas no permitan accesos no autorizados fuera del directorio base
 * configurado.
 *
 * <p>Esta clase realiza validaciones para evitar ataques de path traversal y abre archivos usando
 * codificación UTF-8.
 *
 * @author juan
 * @since 2024-07-18
 */
@Slf4j
public class LocalFeedSource implements FeedSource {

  private final OpenDataExecutionContext context;
  private final PropertiesManagerService propertyManager;

  public LocalFeedSource(OpenDataExecutionContext context) {
    this.context = Objects.requireNonNull(context, "context");
    this.propertyManager = PropertiesManagerServiceImpl.getInstance();
  }

  /**
   * Obtiene la ruta completa al archivo inicial de feed, resolviendo la ruta base configurada y el
   * nombre de archivo definido en las propiedades de la aplicación.
   *
   * @return La ruta absoluta y normalizada al archivo inicial como String.
   * @throws PropertiesManagerException si ocurre algún error al obtener las propiedades o si la
   *     ruta resuelta no está dentro del directorio base permitido (por seguridad).
   */
  @Override
  public String getInitialLink() throws PropertiesManagerException {
    try {
      return FeedLocationHelper.getInitialLink(
          context.getLugarImportacion(), context.getTipoSindicacion());
    } catch (PropertiesManagerException ex) {
      throw new IllegalStateException("No se pudo resolver el feed local inicial", ex);
    }
  }

  /**
   * Comprueba si un enlace (ruta de archivo) es válido, delegando la verificación al helper {@link
   * FileHelper}.
   *
   * @param link Ruta o enlace a validar.
   * @return {@code true} si el archivo existe y es válido; {@code false} en caso contrario.
   */
  @Override
  public boolean isNextLinkValid(String link) {
    return FileHelper.esFileValido(link);
  }

  /**
   * Obtiene la ruta completa al siguiente archivo de feed, partiendo de la ruta base configurada y
   * del valor de enlace siguiente proporcionado por el objeto {@link Feed}.
   *
   * @param feed Objeto {@link Feed} que contiene la referencia al siguiente archivo de feed.
   * @return La ruta absoluta y normalizada al archivo siguiente como String.
   * @throws IOException si ocurre algún error al resolver la ruta o si la ruta no está dentro del
   *     directorio base permitido (por seguridad).
   */
  @Override
  public String getNextLink(Feed feed) throws IOException {

    TipoSindicacion tipo = context.getTipoSindicacion();

    Path basePath = getPathBaseLocal(tipo);
    String linkNext = feed.getLinkNext();
    if (linkNext == null || linkNext.isBlank()) {
      // decide qué hacer: devolver basePath, null, o lanzar excepción
      return basePath.toString();
    }

    Path resolved = basePath.resolve(linkNext).normalize();

    if (!resolved.startsWith(basePath)) {
      throw new IOException("Ruta no permitida para siguiente archivo: " + resolved);
    }

    log.debug("[getNextLink] Ruta resuelta para next link: {}", resolved);
    return resolved.toString();
  }

  /**
   * Abre un {@link BufferedReader} para el archivo especificado usando codificación UTF-8.
   *
   * @param path Ruta del archivo a abrir.
   * @return Un {@link BufferedReader} para leer el archivo.
   * @throws IOException Si el archivo no existe o no puede abrirse.
   */
  @Override
  public BufferedReader openBufferedReader(String path) throws IOException {
    Path readableFile = FileHelper.requireReadableFile(path);
    log.debug("[openBufferedReader] Abriendo archivo: {}", readableFile);
    return Files.newBufferedReader(readableFile, StandardCharsets.UTF_8);
  }

  /**
   * Obtiene la ruta base local definida en el archivo de propiedades, normalizada y convertida a
   * una ruta absoluta para evitar problemas de rutas relativas o acceso no autorizado.
   *
   * @return La ruta base local como objeto {@link Path}.
   * @throws PropertiesManagerException Si no se puede obtener o procesar la propiedad de ruta base.
   */
  private Path getPathBaseLocal(TipoSindicacion tipo) throws PropertiesManagerException {

    String basePath =
        propertyManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_LOCAL_PATH);
    String pathAux = "";

    switch (tipo) {
      case MAYORES -> pathAux = basePath + Constantes.DIRECTORIO_MAY;
      case MENORES -> pathAux = basePath + Constantes.DIRECTORIO_MEN;
      case ENCARGOS -> pathAux = basePath + Constantes.DIRECTORIO_EMP;
      case CONSULTAS -> pathAux = basePath + Constantes.DIRECTORIO_CPM;
      case AGREGRADAS -> pathAux = basePath + Constantes.DIRECTORIO_AGR;
    }

    Path path = Path.of(pathAux).toAbsolutePath().normalize();
    log.debug("[getPathBaseLocal] Ruta base local: {}", path);
    return path;
  }
}
