package local.jarios.exceptions;

/**
 * Excepción personalizada que representa errores relacionados con operaciones de entrada/salida
 * (I/O).
 * <p>
 * Esta excepción se lanza típicamente cuando ocurre un error al leer o escribir archivos, acceder a
 * recursos externos (como Excel o conexiones remotas), o durante la manipulación de flujos de
 * datos.
 * </p>
 *
 * <p><strong>Uso recomendado:</strong> Capturar excepciones {@link java.io.IOException} u otras
 * relacionadas
 * y envolverlas en esta clase para proporcionar un contexto más claro dentro de la aplicación.</p>
 *
 * @author Juan
 * @since 28/12/2024
 */
public class MiIoException extends RuntimeException {

  /**
   * Crea una nueva instancia de {@code MiIoException} con un mensaje descriptivo y la causa
   * original.
   *
   * @param message Mensaje que describe el error ocurrido.
   * @param cause   Causa original del error, típicamente una excepción de I/O.
   */
  public MiIoException(String message, Throwable cause) {
    super(message, cause);
  }
}
