package local.jarios.exceptions;

import java.net.UnknownHostException;

/**
 * Description:
 * Author: juan
 * Date: 28/12/2024
 * Team:
 */
public class MiUnknownHostException extends Exception {

    public MiUnknownHostException(UnknownHostException ex) {

        super(ex);
    }
}
