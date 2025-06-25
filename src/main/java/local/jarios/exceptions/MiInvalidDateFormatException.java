package local.jarios.exceptions;

/**
 * Description:
 * Author: juan
 * Date: 28/12/2024
 * Team:
 */
public class MiInvalidDateFormatException extends RuntimeException {

    public MiInvalidDateFormatException(String message) {

        super(message);
    }

    public MiInvalidDateFormatException(String message, Throwable ex) {

        super(message, ex);
    }
}
