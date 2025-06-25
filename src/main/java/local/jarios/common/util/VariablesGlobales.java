package local.jarios.common.util;

import local.jarios.entity.atom.Entry;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: Variables globales del proyecto
 * Author: juan
 * Date: 16/03/2024
 * Team: Juan Antonio Ríos
 */
public final class VariablesGlobales {

    @Getter
    @Setter
    private static Map<String, String> mapFiltro = new HashMap<>();

    @Getter
    @Setter
    private static Map<String, Entry> mapBaseDatos = new HashMap<>();

    @Getter
    @Setter
    private static boolean existenFiltros;

    @Getter
    @Setter
    private static String schemaName;

    private VariablesGlobales() { }

}
