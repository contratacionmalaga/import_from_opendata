package local.jarios.parsers;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.entity.atom.Feed;
import local.jarios.exceptions.MiUrlException;
import local.jarios.interfaces.FeedSource;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * RemoteFeedSource implementa FeedSource leyendo feeds remotos via URL.
 * Mejora:
 *   - Validación centralizada de URLs
 *   - Evita uso de constructor URL(String) deprecated
 *   - Logging claro y manejo de errores robusto
 *   - isNextLinkValid captura enlaces inválidos sin fallar
 * Author: juan
 * Date: 04/07/2025 (mejorado 05/07/2025)
 */
@Slf4j
public class RemoteFeedSource implements FeedSource {

    /** Servicio centralizado para la gestión de properties */
    private final PropertiesManagerService propertyManager;

    /** Constructor por defecto usando la implementación singleton */
    public RemoteFeedSource() {
        this.propertyManager = PropertiesManagerServiceImpl.getInstance();
    }

    @Override
    public String getInitialLink() throws PropertiesManagerException, MiUrlException {
        String url = propertyManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_URL);
        log.debug("[getInitialLink] URL desde propiedades → {}", url);
        validateUrl(url);
        return url;
    }

    @Override
    public boolean isNextLinkValid(String link) {
        log.debug("[isNextLinkValid] Validando nextLink → {}", link);
        try {
            validateUrl(link);
            return true;
        } catch (MiUrlException e) {
            log.warn("[isNextLinkValid] NextLink inválido: {}", link);
            return false;
        }
    }

    @Override
    public String getNextLink(Feed feed) throws MiUrlException {
        String url = feed.getLinkNext();
        log.debug("[getNextLink] nextLink del Feed '{}': {}", feed.toStringResumido(), url);
        validateUrl(url);
        return url;
    }

    @Override
    public BufferedReader openBufferedReader(String path) throws MiUrlException {
        validateUrl(path);
        try {
            URI raw = new URI(path);
            URI uri = raw.parseServerAuthority(); // usamos el resultado
            URL url = uri.toURL();
            log.debug("[openBufferedReader] Abriendo BufferedReader para URL → {}", uri);
            return new BufferedReader(
                    new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            String msg = "[openBufferedReader] Error al abrir BufferedReader para URL: " + path;
            log.error(msg, e);
            throw new MiUrlException(msg, e);
        }
    }

    /**
     * Valida URL según RFC:
     *  - no nula/vacía
     *  - sintaxis legal
     *  - autoridad válida
     * Si falla, lanza MiUrlException con causa y mensaje.
     */
    private static void validateUrl(String url) throws MiUrlException {
        if (url == null || url.isBlank()) {
            String msg = "[validateUrl] URL nula o en blanco";
            log.error(msg);
            throw new MiUrlException(msg);
        }

        try {
            URI uri = new URI(url).parseServerAuthority();
            URL validatedUrl = uri.toURL(); // usamos el resultado
            log.debug("[validateUrl] URL válida (convertida a URL): {}", validatedUrl);
        } catch (Exception e) {
            String msg = "[validateUrl] URL inválida: " + url;
            log.error(msg, e);
            throw new MiUrlException(msg, e);
        }
    }
}
