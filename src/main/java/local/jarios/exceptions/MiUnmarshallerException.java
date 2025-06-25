package local.jarios.exceptions;

import javax.xml.bind.JAXBException;

/**
 * Description:
 * Author: juan
 * Date: 28/12/2024
 * Team:
 */
public class MiUnmarshallerException extends Exception {

    public MiUnmarshallerException(String ex) {

        super(ex);
    }

    public MiUnmarshallerException(JAXBException ex) {

        super(ex);
    }
}
