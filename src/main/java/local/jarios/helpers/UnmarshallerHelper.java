package local.jarios.helpers;

import local.jarios.exceptions.MiUnmarshallerException;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * @author Juan Antonio
 */
@Slf4j
public final class UnmarshallerHelper {

    private UnmarshallerHelper() {/* CONSTURCTOR VACÍO */}

    /**
     * Obtiene un JAXBContext configurado para las entidades de Feed.
     *      * Utiliza un patrón Singleton para evitar la creación repetitiva del JAXBContext.
     *      * Si ocurre un error durante la creación, lo captura y lo registra.
     *
     * @return Un objeto JAXBContext configurado
     */
    private static JAXBContext getJAXBContext() throws MiUnmarshallerException {

        //
        try {

            //
            var contextPath = String.join(":",
                    Constantes.JAXB_ATOM,
                    Constantes.JAXB_ORG_DGPE_CODICE_COMMON_CACLIB,
                    Constantes.JAXB_ORG_DGPE_CODICE_COMMON_CBCLIB,
                    Constantes.JAXB_EXT_PLACE_CODICE_COMMON_CACLIB,
                    Constantes.JAXB_EXT_PLACE_CODICE_COMMON_CBCLIB,
                    Constantes.JAXB_TOMBSTONES);

            //
            return JAXBContext.newInstance(contextPath);

        } catch (JAXBException ex) {

            //
            var mensajeError = "Error crítico al crear el obteneer una instancia de JAXBContext.";
            log.error(mensajeError);

            //
            throw new MiUnmarshallerException(ex);
        }
    }

    /**
     * Función encargada de devolver el modelo para el parseo de los ficheros ATOM.
     * @return Unmarshaller Lógica necesaria para convertir los ficheros ATOM en objetos JAVA
     */
    public static Unmarshaller getUnmarshaller() throws MiUnmarshallerException{

        //
        try {

            // Obtenemos el contexto JAXB de forma eficiente (singleton)
            var context = getJAXBContext();

            //
            return context.createUnmarshaller();

        } catch (JAXBException ex) {

            //
            var mensajeError = "Error al crear el Unmarshaller a partir de un JAXBContext.";
            log.error(mensajeError);

            //
            throw new MiUnmarshallerException(ex);
        }
    }
}
