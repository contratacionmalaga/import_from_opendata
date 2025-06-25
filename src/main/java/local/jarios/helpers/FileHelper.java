package local.jarios.helpers;

import local.jarios.common.util.Mensajes;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * @author Juan Antonio
 */
@Slf4j
public final class FileHelper {

    private FileHelper() { }

    /**
     * Analiza si un String que se pasa es un File válido (EXISTE, SE PUEDA LEER, .entity..)
     *
     * @param strPathFichero Fichero con la ruta absoluta
     * @return Devuelve un valor indicando si el fichero es valido y en caso contrario indica el motivo
     */
    public static boolean esFileValido(String strPathFichero) {


        //
        File filePathFichero = new File(strPathFichero);

        // Verificación de existencia del archivo
        if (!filePathFichero.exists()) {
            log.error(Mensajes.FILE_NOT_EXIST, strPathFichero);
            return false;
        }

        // Verificación de si es un archivo
        if (!filePathFichero.isFile()) {
            log.error(Mensajes.NOT_FILE, strPathFichero);
            return false;
        }

        // Verificación de permisos de lectura
        if (!filePathFichero.canRead()) {
            log.error(Mensajes.FILE_NOT_READ, strPathFichero);
            return false;
        }

        return true;
    }
}
