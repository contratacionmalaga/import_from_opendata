package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ProcessJustification;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ProcessJustificationAdapter() implements JsonSerializer<ProcessJustification> {

    @Override
    public JsonElement serialize(
            ProcessJustification processJustification,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("ReasonCode", processJustification.getReasonCode());
        jsonObject.addProperty("Description", processJustification.getDescription());

        return jsonObject;
    }
}
