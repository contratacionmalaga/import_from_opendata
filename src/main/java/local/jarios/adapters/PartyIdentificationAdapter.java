package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.PartyIdentification;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record PartyIdentificationAdapter() implements JsonSerializer<PartyIdentification> {

    @Override
    public JsonElement serialize(
            PartyIdentification partyIdentification,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(jsonObject,"IdPlataforma",partyIdentification.getIdPlataforma());

        JsonSerializationHelper
                .addProperty(jsonObject,"Dir3",partyIdentification.getDir3());

        JsonSerializationHelper
                .addProperty(jsonObject,"Nif",partyIdentification.getNif());

        JsonSerializationHelper
                .addProperty(jsonObject,"IdOcPlat",partyIdentification.getIdOcPlat());

        JsonSerializationHelper
                .addProperty(jsonObject,"Otros",partyIdentification.getOtros());

        //
        return jsonObject;
    }
}
