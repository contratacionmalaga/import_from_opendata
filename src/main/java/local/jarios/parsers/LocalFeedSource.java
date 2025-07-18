package local.jarios.parsers;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.FileHelper;
import local.jarios.interfaces.FeedSource;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementación local de {@link FeedSource} que obtiene los feeds desde archivos en disco.
 * <p>
 * Utiliza un servicio centralizado de propiedades para resolver rutas y nombres de archivo,
 * asegurando que las rutas no permitan accesos no autorizados fuera del directorio base configurado.
 * </p>
 *
 * <p>Esta clase realiza validaciones para evitar ataques de path traversal y abre archivos usando
 * codificación UTF-8.</p>
 *
 * @author juan
 * @since 2024-07-18
 */
@Slf4j
public class LocalFeedSource implements FeedSource {

    /**
     * Servicio centralizado para la gestión y lectura de propiedades de configuración.
     */
    private final PropertiesManagerService propertyManager;

    /**
     * Constructor por defecto que obtiene la instancia singleton del gestor de propiedades.
     */
    public LocalFeedSource() {
        this.propertyManager = PropertiesManagerServiceImpl.getInstance();
    }

    /**
     * Obtiene la ruta completa al archivo inicial de feed, resolviendo la ruta base configurada
     * y el nombre de archivo definido en las propiedades de la aplicación.
     *
     * @return La ruta absoluta y normalizada al archivo inicial como String.
     * @throws PropertiesManagerException si ocurre algún error al obtener las propiedades o si la ruta resuelta
     *                   no está dentro del directorio base permitido (por seguridad).
     */
    @Override
    public String getInitialLink() throws PropertiesManagerException {
        String filename = propertyManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_FILENAME);
        Path basePath = getPathBaseLocal();
        Path resolved = basePath.resolve(filename).normalize();

        if (!resolved.startsWith(basePath)) {
            throw new SecurityException("Ruta no permitida para archivo inicial: " + resolved);
        }

        log.debug("[getInitialLink] Ruta resuelta: {}", resolved);
        return resolved.toString();
    }

    /**
     * Comprueba si un enlace (ruta de archivo) es válido, delegando la verificación al helper {@link FileHelper}.
     *
     * @param link Ruta o enlace a validar.
     * @return {@code true} si el archivo existe y es válido; {@code false} en caso contrario.
     */
    @Override
    public boolean isNextLinkValid(String link) {
        return FileHelper.esFileValido(link);
    }

    /**
     * Obtiene la ruta completa al siguiente archivo de feed, partiendo de la ruta base configurada
     * y del valor de enlace siguiente proporcionado por el objeto {@link Feed}.
     *
     * @param feed Objeto {@link Feed} que contiene la referencia al siguiente archivo de feed.
     * @return La ruta absoluta y normalizada al archivo siguiente como String.
     * @throws IOException si ocurre algún error al resolver la ruta o si la ruta no está dentro
     *                   del directorio base permitido (por seguridad).
     */
    @Override
    public String getNextLink(Feed feed) throws IOException {
        Path basePath = getPathBaseLocal();
        Path resolved = basePath.resolve(feed.getLinkNext()).normalize();

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
        log.debug("[openBufferedReader] Abriendo archivo: {}", path);
        return new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
    }

    /**
     * Obtiene la ruta base local definida en el archivo de propiedades, normalizada y convertida
     * a una ruta absoluta para evitar problemas de rutas relativas o acceso no autorizado.
     *
     * @return La ruta base local como objeto {@link Path}.
     * @throws PropertiesManagerException Si no se puede obtener o procesar la propiedad de ruta base.
     */
    private Path getPathBaseLocal() throws PropertiesManagerException {
        String basePath = propertyManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_PATH);
        Path path = Paths.get(basePath).toAbsolutePath().normalize();
        log.debug("[getPathBaseLocal] Ruta base local: {}", path);
        return path;
    }
}
