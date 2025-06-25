package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Country;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record CountryAdapter() implements JsonSerializer<Country> {

    @Override
    public JsonElement serialize(
            Country country,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("IdentificationCode", country.getIdentificationCode());
        jsonObject.addProperty("Name", country.getName());

        //
        return jsonObject;
    }
}
