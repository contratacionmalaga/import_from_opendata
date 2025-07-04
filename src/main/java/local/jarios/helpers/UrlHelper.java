package local.jarios.helpers;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Utilidad para validar URLs según configuración definida en properties.
 */
@Slf4j
public final class UrlHelper {

    private static final String MSG_ESQUEMA_INVALIDO = "Esquema incorrecto. Esperado: {}, Obtenido: {}";
    private static final String MSG_HOST_INVALIDO = "Host incorrecto. Esperado: {}, Obtenido: {}";
    private static final String MSG_PATH_INVALIDO = "Path incorrecto. Esperado que comience con: {}, Obtenido: {}";

    private UrlHelper() {}

    public static boolean esUrlValida(String url) throws PropertiesManagerException, URISyntaxException {
        if (StringHelper.isInvalidString(url)) {
            log.error("[esUrlValida] - URL es nula o vacía.");
            return false;
        }

        URI uri;
        try {
            uri = new URI(url);
            log.debug("[esUrlValida] - Procesando URL: {}", url);
        } catch (URISyntaxException e) {
            log.error("[esUrlValida] - URL con sintaxis inválida: {}", url, e);
            throw e;
        }

        String expectedScheme = getProperty(PropertiesFiles.VALIDATION, PropertiesKeys.VALIDATION_URI_SCHEME);
        String expectedHost = getProperty(PropertiesFiles.VALIDATION, PropertiesKeys.VALIDATION_URI_HOST);
        String expectedPathPrefix = getProperty(PropertiesFiles.VALIDATION, PropertiesKeys.VALIDATION_URI_PATH);

        if (!expectedScheme.equalsIgnoreCase(uri.getScheme())) {
            log.debug(MSG_ESQUEMA_INVALIDO, expectedScheme, uri.getScheme());
            return false;
        }

        if (!expectedHost.equalsIgnoreCase(uri.getHost())) {
            log.debug(MSG_HOST_INVALIDO, expectedHost, uri.getHost());
            return false;
        }

        if (!Objects.toString(uri.getPath(), "").startsWith(expectedPathPrefix)) {
            log.debug(MSG_PATH_INVALIDO, expectedPathPrefix, uri.getPath());
            return false;
        }

        return true;
    }

    public static void validateRemoteUrl(String urlStr) throws PropertiesManagerException, URISyntaxException {
        log.debug("[validateRemoteUrl] - Validando URL: {}", urlStr);

        URI uri;
        try {
            uri = new URI(urlStr);
        } catch (URISyntaxException e) {
            String msg = "[validateRemoteUrl] - URL remota inválida: " + urlStr;
            log.error(msg, e);
            throw e;
        }

        String expectedScheme = getProperty(PropertiesFiles.VALIDATION, PropertiesKeys.VALIDATION_URI_SCHEME);
        String expectedHost = getProperty(PropertiesFiles.VALIDATION, PropertiesKeys.VALIDATION_URI_HOST);

        if (!expectedScheme.equalsIgnoreCase(uri.getScheme())) {
            String msg = String.format("[validateRemoteUrl] - Solo se permiten URLs %s. Encontrado: %s", expectedScheme, uri.getScheme());
            log.error(msg);
            throw new PropertiesManagerException(msg);
        }

        if (uri.getHost() == null || !uri.getHost().endsWith(expectedHost)) {
            String msg = String.format("[validateRemoteUrl] - Host no permitido. Esperado que termine con: %s, Encontrado: %s", expectedHost, uri.getHost());
            log.error(msg);
            throw new PropertiesManagerException(msg);
        }
    }

    private static String getProperty(String file, String key) throws PropertiesManagerException {
        try {
            return PropertiesHelper.getProperty(file, key);
        } catch (Exception e) {
            String msg = String.format ("[UrlHelper] - Error cargando propiedad '%s' del fichero '%s", key, file);
            log.error(msg, e);
            throw new PropertiesManagerException(msg, e);
        }
    }
}
