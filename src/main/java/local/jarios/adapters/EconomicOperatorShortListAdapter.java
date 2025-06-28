package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.EconomicOperatorShortList;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record EconomicOperatorShortListAdapter() implements JsonSerializer<EconomicOperatorShortList> {

    @Override
    public JsonElement serialize(
            EconomicOperatorShortList economicOperatorShorList,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Description",
                        economicOperatorShorList.getDescription());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "ExpectedQuantity",
                        economicOperatorShorList.getExpectedQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "MaximumQuantity",
                        economicOperatorShorList.getMaximumQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "MinimumQuantity",
                        economicOperatorShorList.getMinimumQuantity());

        //
        return jsonObject;
    }
}
