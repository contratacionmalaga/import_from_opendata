package local.jarios.helpers;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.enums.TipoSindicacion;
import local.jarios.common.util.Constantes;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
import java.util.Optional;

/**
 * Ayuda en la obtención del tipo de sindicación (local o remota) a partir de propiedades.
 * Clase utilitaria de solo métodos estáticos y constructor privado.
 *
 * @author Juan
 */
@Slf4j
public final class TipoSindicacionHelper {

    private TipoSindicacionHelper() {
        // No instanciable
    }

    /**
     * Obtiene el nombre del fichero desde la URL (texto tras la última '/').
     *
     * @param url URL completa (no nula)
     * @return Nombre del fichero extraído
     */
    private static String extractFilenameFromUrl(String url) {
        Objects.requireNonNull(url, "La URL no puede ser nula");
        int lastSlash = url.lastIndexOf('/');
        String filename = lastSlash >= 0 ? url.substring(lastSlash + 1) : url;
        log.debug("extractFilenameFromUrl('{}') -> '{}'", url, filename);
        return filename;
    }

    /**
     * Mapea un nombre de fichero a un tipo de sindicación.
     *
     * @param filename nombre del fichero (no nulo ni vacio)
     * @return TipoSindicacion correspondiente, o ERROR si no coincide
     */
    private static TipoSindicacion mapFilenameToTipo(String filename) {
        return Optional.ofNullable(filename)
                .filter(name -> !name.isBlank())
                .map(name -> {
                    return switch (name) {
                        case Constantes.FILENAME_CONSULTASPRELIMINARESMERCADO -> TipoSindicacion.CPM;
                        case Constantes.FILENAME_ENCARGOSMEDIOSPROPIOS -> TipoSindicacion.EMP;
                        case Constantes.FILENAME_MAYORES -> TipoSindicacion.MAY;
                        case Constantes.FILENAME_MENORES -> TipoSindicacion.MEN;
                        case Constantes.FILENAME_AGREGADAS -> TipoSindicacion.AGR;
                        default -> TipoSindicacion.ERROR;
                    };
                })
                .orElse(TipoSindicacion.ERROR);
    }

    private static void assertNotBlank(String str, String errorMsg) throws PropertiesManagerException {
        if (str == null || str.isBlank()) {
            log.error(errorMsg);
            throw new PropertiesManagerException(errorMsg);
        }
    }

    /**
     * Determina el tipo de sindicación local leyendo la propiedad APP_FILENAME.
     *
     * @return Tipo de sindicación según el nombre de fichero en propiedades
     * @throws PropertiesManagerException si la propiedad es inválida o no existe
     */
    public static TipoSindicacion getTipoSindicacionLocal() throws PropertiesManagerException {
        String filename = PropertiesHelper.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_FILENAME);
        log.debug("getTipoSindicacionLocal - archivo: '{}'", filename);
        assertNotBlank(filename, "La propiedad APP_FILENAME no puede estar vacía");
        TipoSindicacion tipo = mapFilenameToTipo(filename);
        log.debug("getTipoSindicacionLocal -> {}", tipo);
        return tipo;
    }

    /**
     * Determina el tipo de sindicación remota leyendo la propiedad APP_URL y extrayendo el
     * nombre del fichero en la URL.
     *
     * @return Tipo de sindicación según el nombre en la URL
     * @throws PropertiesManagerException si la propiedad es inválida o no existe
     */
    public static TipoSindicacion getTipoSindicacionRemota() throws PropertiesManagerException {
        String url = PropertiesHelper.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_URL);
        log.debug("getTipoSindicacionRemota - url: '{}'", url);
        assertNotBlank(url, "La propiedad APP_URL no puede estar vacía");
        String filename = extractFilenameFromUrl(url);
        TipoSindicacion tipo = mapFilenameToTipo(filename);
        log.debug("getTipoSindicacionRemota -> {}", tipo);
        return tipo;
    }
}
