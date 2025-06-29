package local.jarios.helpers;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.enums.TipoSindicacion;
import local.jarios.common.util.Constantes;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Juan Antonio
 */
@Slf4j
public final class TipoSindicacionHelper {


    private TipoSindicacionHelper() {/* CONSTRUCTOR VACÍO */}

    /**
     *
     * @param url url
     * @return String
     */
    private static String getTipoSindicacionByUrl(String url) {

        // Encontrar el índice de la última barra diagonal "/"
        int index = url.lastIndexOf("/");

        // Extraigo lo siguiente a "/"
        return url.substring(index + 1);
    }

    /**
     * Función encargada de devolver el tipo de Sindicación para la importación LOCAL que estamos realizando
     *
     * @param filename Nombre del fichero que recibe
     * @return TipoSindicacion Tipo Enumerado
     */
    private static TipoSindicacion getTipoSindicacionByString(String filename) {

        TipoSindicacion tipoSindicacion;

        //
        switch (filename) {
            case Constantes.URL_FINAL_CONSULTASPRELIMINARESMERCADO -> tipoSindicacion = TipoSindicacion.CPM;
            case Constantes.URL_FINAL_ENCARGOSMEDIOSPROPIOS -> tipoSindicacion = TipoSindicacion.EMP;
            case Constantes.URL_FINAL_MAYORES -> tipoSindicacion = TipoSindicacion.MAY;
            case Constantes.URL_FINAL_MENORES -> tipoSindicacion = TipoSindicacion.MEN;
            case Constantes.URL_FINAL_AGREGADAS -> tipoSindicacion = TipoSindicacion.AGR;
            case Constantes.URL_FINAL_PRUEBAS -> tipoSindicacion = TipoSindicacion.PRUEBA;
            default -> tipoSindicacion = TipoSindicacion.ERROR;
        }

        //
        return tipoSindicacion;
    }

    /**
     * Función encargada de devolver el tipo de Sindicación para la importación LOCAL que estamos realizando
     */
    public static TipoSindicacion getTipoSindicacion(boolean isLocal) throws PropertiesManagerException {

        //
        String config;

        //
        TipoSindicacion tipoSindicacion;

        //
        if (isLocal) {
            config = PropertiesHelper.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_FILENAME);
        } else {
            config = PropertiesHelper.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_URL);
        }

        //
        if (config.isEmpty()) {

            log.error("[getTipoSindicacion] - ]");


        }

        if (isLocal) {
            //
            tipoSindicacion = getTipoSindicacionByString(config);
        } else {
            //
            String resultado = getTipoSindicacionByUrl(config);
            tipoSindicacion = getTipoSindicacionByString(resultado);
        }

        return tipoSindicacion;
    }
}
