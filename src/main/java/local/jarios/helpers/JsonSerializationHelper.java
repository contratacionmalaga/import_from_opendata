package local.jarios.helpers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import java.util.List;

/**
 * Description:
 * Author: juan
 * Date: 03/06/2025
 * Team:
 */
public final class JsonSerializationHelper {

    private JsonSerializationHelper() { } // Prevent instantiation

    public static <T> void addIfNotEmpty(JsonObject jsonObject, String propertyName, List<T> list, JsonSerializationContext context) {
        if (list != null && !list.isEmpty()) {
            jsonObject.add(propertyName, context.serialize(list));
        }
    }

    public static <T> void addIfNotNull(JsonObject jsonObject, String propertyName, T obj, JsonSerializationContext context) {
        if (obj != null) {
            jsonObject.add(propertyName, context.serialize(obj));
        }
    }

    public static void addProperty(JsonObject jsonObject, String propertyName, Object value) {
        if (value == null) return;

        if (value instanceof String strValue) {
            if (!strValue.isBlank()) {
                jsonObject.addProperty(propertyName, strValue);
            }
        } else {
            jsonObject.addProperty(propertyName, String.valueOf(value));
        }
    }
}
