package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.LocatedContractingParty;

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
        jsonObject.addProperty("Id",
                String.valueOf(locatedContractingParty.getId()));
        jsonObject.addProperty("ContractingPartyTypeCode",
                locatedContractingParty.getContractingPartyTypeCode());
        jsonObject.addProperty("BuyerProfileURIID",
                locatedContractingParty.getBuyerProfileUriId());

        if ((imprimirHijos) && (locatedContractingParty.getParty() != null)) {

            jsonObject.add(
                    "Party",
                    context.serialize(locatedContractingParty.getParty()));
        }

        //
        return jsonObject;
    }
}
