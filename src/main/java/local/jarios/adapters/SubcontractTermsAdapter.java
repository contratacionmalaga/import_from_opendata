package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.SubcontractTerms;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(jsonObject,"Rate", subcontractTerms.getRate());

        JsonSerializationHelper
                .addProperty(jsonObject,"Description", subcontractTerms.getDescription());

        //
        return jsonObject;
    }
}
