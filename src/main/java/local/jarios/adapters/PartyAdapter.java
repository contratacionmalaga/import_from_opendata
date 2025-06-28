package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Party;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record PartyAdapter(boolean imprimirHijos) implements JsonSerializer<Party> {

    @Override
    public JsonElement serialize(
            Party party,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("WebsitURI", party.getWebSiteUri());

        //
        JsonSerializationHelper.addIfNotNull(jsonObject, "PartyIdentification", party.getPartyIdentification(), context);

        //
        jsonObject.addProperty("PartyName",party.getPartyName());

        //
        JsonSerializationHelper.addIfNotNull(jsonObject, "PostalAddress", party.getPostalAddress(), context);
        //
        JsonSerializationHelper.addIfNotNull(jsonObject, "PhysicalLocation", party.getPhysicalLocation(), context);
        //
        JsonSerializationHelper.addIfNotNull(jsonObject, "Contact", party.getContact(), context);
        //
        JsonSerializationHelper.addIfNotNull(jsonObject, "AgentParty", party.getAgentParty(), context);


        //
        return jsonObject;
    }
}
