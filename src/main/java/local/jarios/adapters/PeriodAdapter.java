package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Period;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addProperty(jsonObject, "startDateTime", period.getStartDateTime());
        JsonSerializationHelper.addProperty(jsonObject, "endDateTime", period.getEndDateTime());
        JsonSerializationHelper.addProperty(jsonObject, "description", period.getDescription());

        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "DurationMeasure",
                period.getDurationMeasure(),
                context);

        //
        return jsonObject;
    }
}
