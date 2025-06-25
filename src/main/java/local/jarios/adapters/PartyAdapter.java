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
public record PartyAdapter(boolean imprimirHijos) implements JsonSerializer<Party> {

    @Override
    public JsonElement serialize(
            Party party,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("WebsitURI",
                party.getWebSiteUri());
        jsonObject.addProperty("Name",
                party.getPartyName());

        if (imprimirHijos) {

            // PostalAddress
            if (party.getPostalAddress() != null) {
                jsonObject.add(
                        "PostalAddress",
                        context.serialize(party.getPostalAddress()));
            }

            // PhysicalLocation
            if (party.getPhysicalLocation() != null) {
                jsonObject.add(
                        "PhysicalLocation",
                        context.serialize(party.getPhysicalLocation()));
            }

            // Contact
            if (party.getContact() != null) {
                jsonObject.add(
                        "Contact",
                        context.serialize(party.getContact()));
            }

            // AgentParty
            if (party.getAgentParty() != null) {
                jsonObject.add(
                        "AgentParty",
                        context.serialize(party.getAgentParty()));
            }

            // PartyIdentification
            if (party.getPartyIdentification() != null) {
                jsonObject.add(
                        "PartyIdentification",
                        context.serialize(party.getPartyIdentification()));
            }
        }

        //
        return jsonObject;
    }
}
