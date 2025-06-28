package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AwardingCriteria;

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

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("AwardingCriteriaTypeCode", awardingCriteria.getAwardingCriteriaTypeCode());
        jsonObject.addProperty("AwardingCriteriaSubTypeCode", awardingCriteria.getAwardingCriteriaSubTypeCode());
        jsonObject.addProperty("Description", awardingCriteria.getDescription());
        jsonObject.addProperty("Note", awardingCriteria.getNote());
        jsonObject.addProperty("WeightNumeric", awardingCriteria.getWeightNumeric());

        return jsonObject;
    }
}
