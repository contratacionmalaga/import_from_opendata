package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.*;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addProperty(
                jsonObject,
                "PartyName",
                winningParty.getPartyName());

        //
        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "PartyIdentification",
                        winningParty.getPartyIdentification(),
                        context);

        JsonSerializationHelper.
                addIfNotNull(
                        jsonObject,
                        "PhysicalLocation",
                        winningParty.getPhysicalLocation(),
                        context);

        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "Contact",
                        winningParty.getContact(),
                        context);

        JsonSerializationHelper.
                addIfNotNull(
                        jsonObject,
                        "Address",
                        winningParty.getPostalAddress(),
                        context);

        //
        return jsonObject;
    }
}
