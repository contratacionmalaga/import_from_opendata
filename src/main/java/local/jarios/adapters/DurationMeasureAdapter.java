package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Measure;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record DurationMeasureAdapter() implements JsonSerializer<Measure> {

    @Override
    public JsonElement serialize(
            Measure durationMeasure,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Value",
                        durationMeasure.getValue());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "UnitCode",
                        durationMeasure.getUnitCode());

        //
        return jsonObject;
    }
}
