package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ContractExtension;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ContractExtensionAdapter(boolean imprimirHijos) implements JsonSerializer<ContractExtension> {

    @Override
    public JsonElement serialize(
            ContractExtension contractExtension,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addProperty(
                jsonObject,
                "optionsDescription",
                contractExtension.getOptionsDescription());

        //
        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "Period",
                contractExtension.getOptionValidityPeriod(),
                context);

        //
        return jsonObject;
    }
}
