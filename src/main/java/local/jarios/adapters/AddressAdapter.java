package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Address;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record AddressAdapter(boolean imprimirHijos) implements JsonSerializer<Address> {

    @Override
    public JsonElement serialize(
            Address address,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("CityName", address.getCityName());
        jsonObject.addProperty("PostalZone", address.getPostalZone());
        jsonObject.addProperty("AddressLine", address.getAddressLine());

        //
        if (imprimirHijos) {

            //
            jsonObject.add(
                    "Country",
                    context.serialize(address.getCountry()));
        }
        //
        return jsonObject;
    }
}
