package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AwardingTerms;

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

        JsonObject jsonObject = new JsonObject();

        if (!awardingTerms.getListAwardingCriteria().isEmpty()) {

            jsonObject.add(
                    "AwardingCriteria",
                    context.serialize(awardingTerms.getListAwardingCriteria()));
        }

        return jsonObject;
    }
}
