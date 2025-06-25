package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.CommodityClassification;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record CommodityClassificationAdapter() implements JsonSerializer<CommodityClassification> {

    @Override
    public JsonElement serialize(
            CommodityClassification commodityClassification,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id",
                String.valueOf(commodityClassification.getId()));
        jsonObject.addProperty("ItemClassificationCode",
                commodityClassification.getItemClassificationCode());

        return jsonObject;
    }
}
