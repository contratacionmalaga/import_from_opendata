package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Period;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record PeriodAdapter() implements JsonSerializer<Period> {

    @Override
    public JsonElement serialize(
            Period period,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("startDateTime",
                String.valueOf(period.getStartDateTime()));

        jsonObject.addProperty("endDateTime",
                String.valueOf(period.getEndDateTime()));

        jsonObject.addProperty("description",
                String.valueOf(period.getDescription()));

        jsonObject.addProperty("durationMeasureUnitCode",
                String.valueOf(period.getDurationMeasureUnitCode()));

        jsonObject.addProperty("durationMeasureValue",
                String.valueOf(period.getDurationMeasureValue()));

        return jsonObject;
    }
}
