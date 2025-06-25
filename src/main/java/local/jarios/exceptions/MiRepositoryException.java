package local.jarios.exceptions;

/**
 * Description: Excepción pesonalizada
 * @author Juan Antonio
 * @since 28/12/2024
 */
public class MiRepositoryException extends RuntimeException {

    /**
     * Constructor que crea una excepción {@code MiRespositoryException} con un mensaje
     * y una causa especificada.
     *
     * @param message Mensaje descriptivo del error ocurrido.
     * @param cause   Causa original que produjo esta excepción.
     */
    public MiRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor que crea una excepción {@code MiParseException} con solo un mensaje
     * MiRespositoryException
     *
     * @param message Mensaje descriptivo del error.
     */
    public MiRepositoryException(String message) {
        super(message);
    }
}
