package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TenderRecipientParty;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record TenderRecipientPartyAdapter() implements JsonSerializer<TenderRecipientParty> {

    @Override
    public JsonElement serialize(
            TenderRecipientParty tenderRecipientParty, Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "EndPointId",
                        tenderRecipientParty.getEndpointId());

        //
        return jsonObject;
    }
}
