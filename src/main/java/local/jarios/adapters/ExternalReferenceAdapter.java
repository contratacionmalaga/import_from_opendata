package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ExternalReference;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ExternalReferenceAdapter() implements JsonSerializer<ExternalReference> {

    @Override
    public JsonElement serialize(
            ExternalReference externalReference,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addProperty(
                jsonObject,
                "Uri",
                externalReference.getUri());

        JsonSerializationHelper.addProperty(
                jsonObject,
                "DocumentHash",
                externalReference.getDocumentHash());

        JsonSerializationHelper.addProperty(
                jsonObject,
                "AuctionConstraintIndicator",
                externalReference.getFilename());

        //
        return jsonObject;
    }
}
