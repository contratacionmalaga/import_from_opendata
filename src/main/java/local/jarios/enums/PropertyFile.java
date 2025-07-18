package local.jarios.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Determina si el contrato es MAYOR o MENOR
 * Author: juan
 * Date: 03/03/2024
 * Team: Juan Antonio Ríos Peláez
 */
@Getter
public enum PropertyFile {

    //
    PROPERTY_CONFIG("config/app.properties"),

    //
    PROPERTY_FILTER("config/filter.properties"),

    //
    PROPERTY_VALIDATION("config/validation.properties"),

    //
    PROPERTY_HIBERNATE("config/hibernate.properties"),

    //
    PROPERTY_MAIL("config/mail.properties"),

    //
    PROPERTY_RELEASE("release.properties");

    //
    private final String ruta;

    /** Constructor */
    PropertyFile(String ruta) {

        //
        this.ruta = ruta;
    }

    /**
     * Devuelve la lista con todos los ficheros de configuración que se deben procesar
     *
     * @return lista de todos los ficheros de configuración
     */
    public static List<String> getAllFilePaths() {
        List<String> paths = new ArrayList<>();
        for (PropertyFile file : PropertyFile.values()) {
            paths.add(file.getRuta());
        }
        return paths;
    }
}
