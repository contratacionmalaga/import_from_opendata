package local.jarios.helpers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import local.jarios.common.util.Constantes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * Author: juan
 * Date: 03/06/2025
 * Team:
 */
public final class JsonSerializationHelper {

    /**
     * Constructor privado
     */
    private JsonSerializationHelper() { } // Prevent instantiation

    /**
     * Añadir si no es vacío
     * @param jsonObject Donde se añade
     * @param propertyName Propiedad
     * @param list Lista que se comprueba qu eno sea vacía ni null
     * @param context Contexto
     * @param <T> Tipo de Objeto
     */
    public static <T> void addIfNotEmpty(
            JsonObject jsonObject, String propertyName, List<T> list, JsonSerializationContext context) {
        if (list != null && !list.isEmpty()) {
            jsonObject.add(propertyName, context.serialize(list));
        }
    }

    /**
     * Añadir si no es vacío
     * @param jsonObject Donde se añade
     * @param propertyName Propiedad
     * @param map Map que se comprueba qu eno sea vacía ni null
     * @param context Contexto
     * @param <T> Tipo de Objeto
     */
    public static <T> void addIfNotEmptyMap(
            JsonObject jsonObject, String propertyName, Map<T, T> map, JsonSerializationContext context) {
        if (map != null && !map.isEmpty()) {
            jsonObject.add(propertyName, context.serialize(map));
        }
    }

    /**
     * Añadir si no es null
     * @param jsonObject Donde se añade
     * @param propertyName Propiedad
     * @param obj Objeto que se añade en caso de no ser NULL
     * @param context Contexto
     * @param <T> Tipo de Objeto
     */
    public static <T> void addIfNotNull(JsonObject jsonObject, String propertyName, T obj, JsonSerializationContext context) {
        if (obj != null) {
            jsonObject.add(propertyName, context.serialize(obj));
        }
    }

    /**
     * Añadir property. Si es nula devuelve NULL. Si es un String devuelve el String. En caso de no ser un String
     *                  realiza un String.valueOf() del valor.
     * @param jsonObject Donde
     * @param propertyName Propiedad
     * @param value Valor
     */
    public static void addProperty(JsonObject jsonObject, String propertyName, Object value) {
        String propertyValue;
        if (value == null) {
            propertyValue = Constantes.NULL;
        } else {
            propertyValue = switch (value) {
                case Integer intValue -> StringHelper.getNumeroConFormato(intValue);
                case Long lngValue -> StringHelper.getNumeroConFormato(Math.toIntExact(lngValue));
                case String strValue -> strValue;
                case LocalDateTime ldtValue -> ComunHelper.getFechaHoraFormateada(ldtValue);
                default -> String.valueOf(value);
            };
        }
        jsonObject.addProperty(propertyName, propertyValue);
    }
}
