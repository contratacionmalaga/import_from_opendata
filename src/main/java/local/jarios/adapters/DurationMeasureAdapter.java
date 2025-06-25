package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.DurationMeasure;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record DurationMeasureAdapter() implements JsonSerializer<DurationMeasure> {

    @Override
    public JsonElement serialize(
            DurationMeasure durationMeasure,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Value",
                durationMeasure.getValue());
        jsonObject.addProperty("UnitCode",
                durationMeasure.getUnitCode());

        return jsonObject;
    }
}
