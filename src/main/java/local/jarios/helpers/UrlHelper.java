package local.jarios.helpers;

import local.jarios.exceptions.MiUrlException;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilidad para validar URLs según configuración definida en properties.
 */
@Slf4j
public final class UrlHelper {

    private UrlHelper() {}

    public static boolean isUrlValid(String url) throws MiUrlException {
        if (StringHelper.isInvalidString(url)) {
            String msg = "[esUrlValida] - URL es nula o vacía.";
            log.error(msg);
            throw new MiUrlException(msg);
        }
        log.debug("[esUrlValida] - Es válida la URL: {}", url);
        return true;
    }
}
