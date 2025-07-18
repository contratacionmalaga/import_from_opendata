package local.jarios.parsers;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.FileHelper;
import local.jarios.interfaces.FeedSource;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
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
 * Utiliza un servicio de propiedades centralizado para resolver rutas y nombres de archivo.
 */
@Slf4j
public class LocalFeedSource implements FeedSource {

    /** Servicio centralizado para la gestión de properties */
    private final PropertiesManagerService propertyManager;

    /** Constructor por defecto usando la implementación singleton */
    public LocalFeedSource() {
        this.propertyManager = PropertiesManagerServiceImpl.getInstance();
    }

    @Override
    public String getInitialLink() throws Exception {
        String filename = propertyManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_FILENAME);
        Path resolved = getPathBaseLocal().resolve(filename).normalize();
        if (!resolved.startsWith(getPathBaseLocal())) {
            throw new SecurityException("Ruta no permitida para archivo inicial: " + resolved);
        }
        log.debug("[getInitialLink] Ruta resuelta: {}", resolved);
        return resolved.toString();
    }

    @Override
    public boolean isNextLinkValid(String link) {
        return FileHelper.esFileValido(link);
    }

    @Override
    public String getNextLink(Feed feed) throws Exception {
        Path resolved = getPathBaseLocal().resolve(feed.getLinkNext()).normalize();
        if (!resolved.startsWith(getPathBaseLocal())) {
            throw new SecurityException("Ruta no permitida para siguiente archivo: " + resolved);
        }
        log.debug("[getNextLink] Ruta resuelta para next link: {}", resolved);
        return resolved.toString();
    }

    @Override
    public BufferedReader openBufferedReader(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
    }

    /**
     * Obtiene la ruta base local definida en el archivo de propiedades.
     */
    private Path getPathBaseLocal() throws Exception {
        String basePath = propertyManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_PATH);
        Path path = Paths.get(basePath).toAbsolutePath().normalize();
        log.debug("[getPathBaseLocal] Ruta base local: {}", path);
        return path;
    }
}
