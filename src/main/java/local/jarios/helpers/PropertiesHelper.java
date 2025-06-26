package local.jarios.helpers;

import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Clase utilitaria para operaciones relacionadas con tiempo y fechas.
 * <p>
 * Proporciona métodos para obtener la fecha y hora actual en zonas horarias específicas.
 * </p>
 *
 * Author: juan
 * Date: 21/06/2025
 */
@Slf4j
public final class PropertiesHelper {


    /**
     * Constructor privado para evitar instanciación.
     */
    private PropertiesHelper() { }

    /**
     * Obtiene la fecha y hora local actual en la zona horaria de Madrid (Europe/Madrid).
     *
     * @return {@link LocalDateTime} con la fecha y hora actual en Madrid.
     */
    public static String getProperty(String propertyFile, String propertyKey) throws PropertiesManagerException {

        try {
            PropertiesManagerService propertiesManagerService = PropertiesManagerServiceImpl.getInstance();
            return propertiesManagerService.getProperty(propertyFile, propertyKey);
        } catch (PropertiesManagerException ex) {
            String msg = String.format(
                    "[getProperty] - Excepción leyecto la propiedad: '%s' del fichero: `%s'. Error: %s",
                    propertyKey,
                    propertyFile,
                    ex.getMessage());
            log.error(msg, ex);
            throw ex;
        }
    }
}
