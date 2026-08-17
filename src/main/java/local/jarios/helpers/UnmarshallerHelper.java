package local.jarios.helpers;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import local.jarios.common.util.Constantes;
import local.jarios.exceptions.MiUnmarshallerException;
import lombok.extern.slf4j.Slf4j;

/**
 *
 *
 * <h2>Helper para crear y obtener instancias de {@link Unmarshaller}</h2>
 *
 * <p>Esta clase se encarga de centralizar la lógica necesaria para la creación de un {@link
 * JAXBContext} y, a partir de este, un {@link Unmarshaller}. Se utiliza un patrón Singleton
 * implícito para evitar recrear el contexto en cada invocación.
 *
 * <p><b>Características principales:</b>
 *
 * <ul>
 *   <li>Provee un método seguro para obtener un {@link JAXBContext} configurado.
 *   <li>Controla las excepciones {@link JAXBException} y las transforma en {@link
 *       MiUnmarshallerException}.
 *   <li>Permite obtener un {@link Unmarshaller} listo para parsear ficheros ATOM.
 * </ul>
 *
 * @author Juan Antonio
 */
@Slf4j
public final class UnmarshallerHelper {

  /**
   * Constructor privado para evitar instanciación. Esta clase es utilitaria y solo expone métodos
   * estáticos.
   */
  private UnmarshallerHelper() {
    /* Constructor vacío para evitar instanciación */
  }

  /**
   * Obtiene un {@link JAXBContext} configurado para las entidades de Feed.
   *
   * <p>Este método concatena todos los contextos de paquetes necesarios definidos en {@link
   * Constantes}. Si ocurre un error durante la creación del contexto, lo captura, lo registra en
   * logs y lo encapsula en una {@link MiUnmarshallerException}.
   *
   * @return Un objeto {@link JAXBContext} configurado con los paquetes requeridos.
   * @throws MiUnmarshallerException si ocurre un error al crear la instancia del contexto.
   */
  private static JAXBContext getJAXBContext() throws MiUnmarshallerException {
    try {
      var contextPath =
          String.join(
              ":",
              Constantes.JAXB_ATOM,
              Constantes.JAXB_ORG_DGPE_CODICE_COMMON_CACLIB,
              Constantes.JAXB_ORG_DGPE_CODICE_COMMON_CBCLIB,
              Constantes.JAXB_EXT_PLACE_CODICE_COMMON_CACLIB,
              Constantes.JAXB_EXT_PLACE_CODICE_COMMON_CBCLIB,
              Constantes.JAXB_TOMBSTONES);

      return JAXBContext.newInstance(contextPath);
    } catch (JAXBException ex) {
      var mensajeError = "Error crítico al obtener una instancia de JAXBContext.";
      log.error(mensajeError, ex);
      throw new MiUnmarshallerException(ex);
    }
  }

  /**
   * Devuelve un {@link Unmarshaller} configurado para parsear ficheros ATOM.
   *
   * <p>Este método reutiliza el {@link JAXBContext} creado en {@link #getJAXBContext()}, asegurando
   * eficiencia en el proceso de creación de unmarshallers.
   *
   * @return Un objeto {@link Unmarshaller} listo para convertir ficheros ATOM en objetos Java.
   * @throws MiUnmarshallerException si ocurre un error al instanciar el {@link Unmarshaller}.
   */
  public static Unmarshaller getUnmarshaller() throws MiUnmarshallerException {
    try {
      var context = getJAXBContext();
      return context.createUnmarshaller();
    } catch (JAXBException ex) {
      var mensajeError = "Error al crear el Unmarshaller a partir de un JAXBContext.";
      log.error(mensajeError, ex);
      throw new MiUnmarshallerException(ex);
    }
  }
}
