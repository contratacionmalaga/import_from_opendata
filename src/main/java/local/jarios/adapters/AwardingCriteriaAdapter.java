package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AwardingCriteria;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record AwardingCriteriaAdapter() implements JsonSerializer<AwardingCriteria> {

    @Override
    public JsonElement serialize(
            AwardingCriteria awardingCriteria,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "AwardingCriteriaTypeCode",
                        awardingCriteria.getAwardingCriteriaTypeCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "AwardingCriteriaSubTypeCode",
                        awardingCriteria.getAwardingCriteriaSubTypeCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Description",
                        awardingCriteria.getDescription());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Note",
                        awardingCriteria.getNote());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "WeightNumeric",
                        awardingCriteria.getWeightNumeric());

        //
        return jsonObject;
    }
}
