package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.LocatedContractingParty;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record LocatedContractingPartyAdapter(boolean imprimirHijos)
        implements JsonSerializer<LocatedContractingParty> {

    @Override
    public JsonElement serialize(
            LocatedContractingParty locatedContractingParty,
            Type typeOfSrc,
            JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("ContractingPartyTypeCode", locatedContractingParty.getContractingPartyTypeCode());
        jsonObject.addProperty("BuyerProfileURIID", locatedContractingParty.getBuyerProfileUriId());

        //
        JsonSerializationHelper.addIfNotNull(jsonObject, "Party", locatedContractingParty.getParty(), context);

        //
        return jsonObject;
    }
}
