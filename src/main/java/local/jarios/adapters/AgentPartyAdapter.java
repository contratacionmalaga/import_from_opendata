package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Party;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record AgentPartyAdapter(boolean imprimirHijos) implements JsonSerializer<Party> {

    @Override
    public JsonElement serialize(
            Party party,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("WebsitURI", party.getWebSiteUri());
        jsonObject.addProperty("Name", party.getPartyName());

        //
        if (imprimirHijos && party.getPartyIdentification() != null) {
            jsonObject.add("PartyIdentification", context.serialize(party.getPartyIdentification()));
        }

        //
        return jsonObject;
    }
}
