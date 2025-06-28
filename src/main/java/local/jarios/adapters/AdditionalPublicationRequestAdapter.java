package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AdditionalPublicationRequest;

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

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("AgencyId",
                additionalPublicationRequest.getAgencyId());
        jsonObject.addProperty("SendDateTime",
                String.valueOf(additionalPublicationRequest.getSendDateTime()));

        return jsonObject;
    }
}
