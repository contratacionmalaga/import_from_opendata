package local.jarios.helpers;

import local.jarios.models.FiltroOrganoContratacion;
import local.jarios.common.util.Constantes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Juan Antonio
 */
public final class MapHelper {

    private MapHelper() { }

    public static <K, V> String getStringFromMap(Map<K,V> map ) {

        //
        var concatenatedString = new StringBuilder();

        //
        String registro;

        // Iterar sobre las entradas del mapa
        for (Map.Entry<K, V> entry : map.entrySet()) {

            //
            registro = entry.getKey().toString() + "; " + entry.getValue().toString() + Constantes.CR;

            // Concatenar el valor de cada entrada
            concatenatedString.append(registro);
        }

        //
        return concatenatedString.toString();
    }

    public static Map<String, String> getMapFromList (
            List<FiltroOrganoContratacion> listFiltroOrganoContratacion) {

        //
        if (listFiltroOrganoContratacion == null || listFiltroOrganoContratacion.isEmpty()) {

            // Retorna un mapa vacío si la lista es nula o vacía
            return Collections.emptyMap();
        }

        // Devuelvo el mapa
        return listFiltroOrganoContratacion.stream().collect(
                Collectors.toMap(
                        FiltroOrganoContratacion::getIdPlataforma,
                        FiltroOrganoContratacion::getNombreOrganoContratacion));
    }
}
