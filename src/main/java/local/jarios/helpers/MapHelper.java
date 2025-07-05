package local.jarios.helpers;

import local.jarios.interfaces.HasIdEntry;
import local.jarios.models.FiltroOrganoContratacion;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Juan Antonio
 */
@Slf4j
public final class MapHelper {

    private MapHelper() { }

    /**
     * Validador de maps
     * @param map mapa a validar
     * @return booleano con la respuesta
     * @param <K> Tipo asociado al Key del mapa
     * @param <V> Tipo asociado al Value del mapa
     */
    public static <K,V> boolean isMapInvalid(Map<K,V> map ) {

        return (map == null);
    }

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

    public static <K, V> void printMap(Map<K, V> map) {

        // Usamos forEach para recorrer el mapa y llamar a printKeyValue
        map.forEach(MapHelper::printKeyValue);
    }

    // Método auxiliar para imprimir clave y valor
    public static <K, V> void printKeyValue(K key, V value) {

        log.info("{} | {}", key, value.toString());
    }


    public static <K, V extends HasIdEntry<V>> int analisisMap (Map<K, V> map) {

        //
        if (isMapInvalid(map)) {
            log.info("[analisisMap] - Map no válido.");
        }

        //
        AtomicInteger nErrores = new AtomicInteger();

        //
        map.forEach((key,value) -> {
            if (!key.equals(value.getIdEntry())) {
                log.info("[analisisMap] - ERROR!!! Key: '{}' | Value: {}", key, value.getIdEntry());
                nErrores.getAndIncrement();
            }
        });

        //
        return nErrores.get();
    }
}
