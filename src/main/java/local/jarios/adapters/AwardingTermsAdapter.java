package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AwardingTerms;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record AwardingTermsAdapter(boolean imprimirHijos) implements JsonSerializer<AwardingTerms> {

    @Override
    public JsonElement serialize(
            AwardingTerms awardingTerms,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addIfNotEmpty(
                jsonObject,
                "List<AwardingCriteria>",
                awardingTerms.getListAwardingCriteria(),
                context);

        //
        return jsonObject;
    }
}
