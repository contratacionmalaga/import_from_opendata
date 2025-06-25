package local.jarios.exceptions;

import java.net.URISyntaxException;

/**
 * Description:
 * Author: juan
 * Date: 28/12/2024
 * Team:
 */
public class MiUrlException extends Exception {

    public MiUrlException(URISyntaxException ex) {

        super(ex);
    }
}
