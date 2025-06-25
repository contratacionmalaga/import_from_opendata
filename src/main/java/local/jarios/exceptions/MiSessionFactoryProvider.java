package local.jarios.exceptions;

/**
 * Description: Excepción pesonalizada
 * @author Juan Antonio
 * @since 28/12/2024
 */
public class MiSessionFactoryProvider extends RuntimeException {

    /**
     * Constructor que crea una excepción {@code MiServiceException} con un mensaje
     * y una causa especificada.
     *
     * @param message Mensaje descriptivo del error ocurrido.
     * @param cause   Causa original que produjo esta excepción.
     */
    public MiSessionFactoryProvider(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor que crea una excepción {@code MiServiceException} con solo un mensaje
     * descriptivo del error ocurrido.
     *
     * @param message Mensaje descriptivo del error.
     */
    public MiSessionFactoryProvider(String message) {
        super(message);
    }
}
