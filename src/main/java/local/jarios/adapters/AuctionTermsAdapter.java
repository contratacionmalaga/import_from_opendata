package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AuctionTerms;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record AuctionTermsAdapter() implements JsonSerializer<AuctionTerms> {

    @Override
    public JsonElement serialize(
            AuctionTerms auctionTerms,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "AuctionConstraintIndicator",
                        auctionTerms.getAuctionConstraintIndicator());

        //
        return jsonObject;
    }
}
