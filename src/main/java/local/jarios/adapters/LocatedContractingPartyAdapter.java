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
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "ContractingPartyTypeCode",
                        locatedContractingParty.getContractingPartyTypeCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "BuyerProfileURIID",
                        locatedContractingParty.getBuyerProfileUriId());

        //
        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "Party",
                        locatedContractingParty.getParty(),
                        context);

        //
        return jsonObject;
    }
}
