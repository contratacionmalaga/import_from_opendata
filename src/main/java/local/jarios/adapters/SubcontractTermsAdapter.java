package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.SubcontractTerms;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record SubcontractTermsAdapter() implements JsonSerializer<SubcontractTerms> {

    @Override
    public JsonElement serialize(
            SubcontractTerms subcontractTerms,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id",
                String.valueOf(subcontractTerms.getId()));
        jsonObject.addProperty("Rate",
                subcontractTerms.getRate());
        jsonObject.addProperty("Description",
                subcontractTerms.getDescription());

        return jsonObject;
    }
}
