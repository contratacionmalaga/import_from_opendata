package local.jarios.helpers;

import local.jarios.common.util.Constantes;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.exceptions.MiUrlException;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Juan Antonio
 */

@Slf4j
public final class UrlHelper {

    private static final String URL_ESQUEMA_MOTIVO_ERROR = "Esquema incorrecto. Esperado: {}, Obtenido: {}";
    private static final String URL_HOST_MOTIVO_ERROR = "Host incorrecto. Esperado: {}, Obtenido: {}";
    private static final String URL_PATH_MOTIVO_ERROR = "Path incorrecto. Esperado: {}, Obtenido: {}";

    private UrlHelper() { }

    /**
     * Imprime los valores de los diferentes ficheros properties.
     */
    public static boolean esUrlValida(String url) throws PropertiesManagerException {

        // Analizo si el valor que paso es NULO | VACÍO.
        if (url == null || url.isBlank()) {

            String msg = "[esUrlValida] - La URL que se ha pasado es NULL | BLANK.";
            log.error(msg);
            return false;
        }

        try {

            var uri = new URI(url);
            log.debug("[esUrlValida] - Procesando la URL: {}.", url);

            // Verifico que el esquema sea HTTPS
            var scheme = PropertiesHelper.getProperty(Constantes.VALIDATION_PROPERTIES, PropertiesKeys.PARAMETRO_URI_SCHEME);
            if (uri.getScheme() == null || !uri.getScheme().equalsIgnoreCase(scheme)) {
                log.debug(URL_ESQUEMA_MOTIVO_ERROR, scheme, uri.getScheme());
                return false;
            }

            // Verifico que el host coincida con el dominio adecuado
            var host = PropertiesHelper.getProperty(Constantes.VALIDATION_PROPERTIES, PropertiesKeys.PARAMETRO_URI_HOST);
            if (uri.getHost() == null || !uri.getHost().equalsIgnoreCase(host)) {
                log.debug(URL_HOST_MOTIVO_ERROR, host, uri.getHost());
                return false;
            }

            // Validación del path (debe comenzar con /sindicacion/)
            var path = PropertiesHelper.getProperty(Constantes.VALIDATION_PROPERTIES, PropertiesKeys.PARAMETRO_URI_PATH);
            if (uri.getPath() == null || !uri.getPath().startsWith(path)) {
                log.debug(URL_PATH_MOTIVO_ERROR, path, uri.getPath());
                return false;
            }

            return true;

        } catch (PropertiesManagerException ex) {

            throw ex;

        }
    }

    public static void validateRemoteUrl(String urlStr) throws PropertiesManagerException, URISyntaxException {

        log.debug("[validateRemoteUrl]: urlStr: {}", urlStr);

        String miScheme = PropertiesHelper.getProperty(Constantes.VALIDATION_PROPERTIES, Constantes.PARAMETRO_URI_SCHEME);
        log.debug("[validateRemoteUrl]: Scheme de validación: {}", miScheme);
        String miHost = PropertiesHelper.getProperty(Constantes.VALIDATION_PROPERTIES, Constantes.PARAMETRO_URI_HOST);
        log.debug("[validateRemoteUrl]: Host de validación: {}", miHost);
        String msg;

        //
        try {

            URI uri = new URI(urlStr);
            log.debug("[validateRemoteUrl] - Uri: {}", uri);
            String scheme = uri.getScheme();
            log.debug("[validateRemoteUrl] - Scheme: {}", scheme);
            String host = uri.getHost();
            log.debug("[validateRemoteUrl] - Host: {}", host);

            if (!miScheme.equalsIgnoreCase(scheme)) {
                msg = String.format ("[validateRemoteUrl] - Solo se permiten URLs HTTPS. Esquema encontrado: %s", scheme);
                log.error(msg);
                throw new PropertiesManagerException(msg);
            }

            // Restringe si quieres el host o dominio
            if (host == null || !host.endsWith(miHost)) {
                msg = String.format ("[validateRemoteUrl] - Solo se permiten URLs HTTPS. Esquema encontrado: %s", scheme);
                log.error(msg);
                throw new PropertiesManagerException("[validateRemoteUrl] - Host no permitido: " + host);
            }

        } catch (URISyntaxException ex) {

            msg = String.format ("[validateRemoteUrl] - URL remota inválida: %s", urlStr);
            log.error(msg, ex);
            throw ex;

        }
    }
}
