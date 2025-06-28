package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Uuid;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record UuidAdapter() implements JsonSerializer<Uuid> {

    @Override
    public JsonElement serialize(Uuid uuid, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("scheme_name", uuid.getSchemeName());
        jsonObject.addProperty("uuid", uuid.getUuid());

        return jsonObject;
    }
}
