package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.PartyIdentification;

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
        jsonObject.addProperty("idPlataforma", partyIdentification.getIdPlataforma());
        jsonObject.addProperty("dir3", partyIdentification.getDir3());
        jsonObject.addProperty("nif", partyIdentification.getNif());
        jsonObject.addProperty("idOcPlat", partyIdentification.getIdOcPlat());
        jsonObject.addProperty("otros", partyIdentification.getOtros());

        //
        return jsonObject;
    }
}
