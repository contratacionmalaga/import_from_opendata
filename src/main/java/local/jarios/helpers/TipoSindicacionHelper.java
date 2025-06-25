package local.jarios.helpers;

import local.jarios.enums.TipoSindicacion;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.Mensajes;
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
     * @param fileName Nombre del fichero que recibe
     * @return TipoSindicacion Tipo Enumerado
     */
    private static TipoSindicacion getTipoSindicacionByString(String fileName) {

        TipoSindicacion tipoSindicacion;

        //
        switch (fileName) {
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
    public static TipoSindicacion getTipoSindicacion(boolean isLocal) {

        // Cargo los ficheros properties utilizando el patrón SINGLETON
        PropertyManager propertyManager = PropertyManager.getInstance();

        //
        String config;

        //
        TipoSindicacion tipoSindicacion;

        //
        if (isLocal) {
            config = propertyManager.getProperty(PropertyConstantes.CONFIG_FILENAME);
        } else {
            config = propertyManager.getProperty(PropertyConstantes.CONFIG_URL);
        }

        //
        if (!config.isEmpty()) {
            if (isLocal) {
                //
                tipoSindicacion = getTipoSindicacionByString(config);
            } else {
                //
                String resultado = getTipoSindicacionByUrl(config);
                tipoSindicacion = getTipoSindicacionByString(resultado);
            }

        } else {

            //
            tipoSindicacion = TipoSindicacion.ERROR;

            //
            var mensajeError = String.format(
                    Mensajes.ERROR_LECTURA_VARIABLE_PROPERTY, PropertyConstantes.CONFIG_FILENAME);

            //
            log.error(mensajeError);

        }

        return tipoSindicacion;
    }
}
