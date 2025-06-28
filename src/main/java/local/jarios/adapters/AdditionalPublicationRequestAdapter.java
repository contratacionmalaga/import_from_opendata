package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AdditionalPublicationRequest;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record AdditionalPublicationRequestAdapter()
        implements JsonSerializer<AdditionalPublicationRequest> {

    @Override
    public JsonElement serialize(
            AdditionalPublicationRequest additionalPublicationRequest,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addProperty(
                jsonObject,
                "AgencyId",
                additionalPublicationRequest.getAgencyId());
        JsonSerializationHelper.addProperty(
                jsonObject,
                "SendDateTime",
                additionalPublicationRequest.getSendDateTime());

        //
        return jsonObject;
    }
}
