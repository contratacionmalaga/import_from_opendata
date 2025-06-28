package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TendererRequirement;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record TendererRequirementAdapter() implements JsonSerializer<TendererRequirement> {

    @Override
    public JsonElement serialize(
            TendererRequirement tendererRequirement,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "RequirementTypeCode",
                        tendererRequirement.getRequirementTypeCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Description",
                        tendererRequirement.getDescription());

        //
        return jsonObject;
    }
}
