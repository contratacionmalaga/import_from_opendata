package local.jarios.helpers;

import local.jarios.exceptions.MiUrlException;
import local.jarios.common.util.Mensajes;
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
    public static boolean esUrlValida(String url) throws MiUrlException {

        // Analizo si el valor que paso es NULO | VACÍO.
        if (url == null || url.isEmpty()) {

            log.error(Mensajes.URL_NULL_EMPTY);
            return false;  // Devuelvo false directamente
        }

        var propertyManager = PropertyManager.getInstance();

        try {

            var uri = new URI(url);

            // Verifico que el esquema sea HTTPS
            var scheme = propertyManager.getProperty(PropertyConstantes.PARAMETRO_URI_SCHEME);
            if (uri.getScheme() == null || !uri.getScheme().equalsIgnoreCase(scheme)) {
                log.error(URL_ESQUEMA_MOTIVO_ERROR, scheme, uri.getScheme());
                return false;
            }

            // Verifico que el host coincida con el dominio adecuado
            var host = propertyManager.getProperty(PropertyConstantes.PARAMETRO_URI_HOST);
            if (uri.getHost() == null || !uri.getHost().equalsIgnoreCase(host)) {
                log.error(URL_HOST_MOTIVO_ERROR, host, uri.getHost());
                return false;
            }

            // Validación del path (debe comenzar con /sindicacion/)
            var path = propertyManager.getProperty(PropertyConstantes.PARAMETRO_URI_PATH);
            if (uri.getPath() == null || !uri.getPath().startsWith(path)) {
                log.error(URL_PATH_MOTIVO_ERROR, path, uri.getPath());
                return false;
            }

            return true;

        } catch (URISyntaxException ex) {
            log.error("Excepción al validar la URL: {}", url, ex);
            throw new MiUrlException(ex);
        }
    }
}
