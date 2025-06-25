package local.jarios.enums;

/**
 * Description: Determina si el contrato es MAYOR o MENOR
 * Author: juan
 * Date: 03/03/2024
 * Team: Juan Antonio Ríos Peláez
 */
public enum LugarImportacion {
    LOCAL,
    INTERNET;

    LugarImportacion() {

    }

    // Ejemplo de método (puede ser un enum o boolean)
    public boolean isLocalImport() {
        return this == LOCAL;
    }
}
