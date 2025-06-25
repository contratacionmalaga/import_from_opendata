package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.WinningParty;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record WinningPartyAdapter(boolean imprimirHijos) implements JsonSerializer<WinningParty> {

    @Override
    public JsonElement serialize(
            WinningParty winningParty,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id",
                String.valueOf(winningParty.getId()));
        jsonObject.addProperty("PartyName",
                winningParty.getPartyName());


        if (imprimirHijos) {

            // PartyIdentification
            if (winningParty.getPartyIdentification() != null) {
                jsonObject.add(
                        "PartyIdentification",
                        context.serialize(winningParty.getPartyIdentification()));
            }

            // PhysicalLocation
            if (winningParty.getPhysicalLocation() != null) {
                jsonObject.add(
                        "PhysicalLocation",
                        context.serialize(winningParty.getPhysicalLocation()));
            }

            // TenderResult
            if (winningParty.getTenderResult() != null) {
                jsonObject.add(
                        "TenderResult",
                        context.serialize(winningParty.getTenderResult()));
            }
        }

        //
        return jsonObject;
    }
}
