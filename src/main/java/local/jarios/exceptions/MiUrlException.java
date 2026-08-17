package local.jarios.exceptions;

/** Description: Author: juan Date: 28/12/2024 Team: */
public class MiUrlException extends Exception {

  /**
   * Constructor que crea una excepción {@code MiServiceException} con un mensaje y una causa
   * especificada.
   *
   * @param message Mensaje descriptivo del error ocurrido.
   * @param cause Causa original que produjo esta excepción.
   */
  public MiUrlException(String message, Throwable cause) {

    super(message, cause);
  }

  /**
   * Constructor que crea una excepción {@code MiServiceException} con solo un mensaje descriptivo
   * del error ocurrido.
   *
   * @param message Mensaje descriptivo del error.
   */
  public MiUrlException(String message) {

    super(message);
  }
}
