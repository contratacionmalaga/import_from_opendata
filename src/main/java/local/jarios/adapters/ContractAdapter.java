package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Contract;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ContractAdapter() implements JsonSerializer<Contract> {

    @Override
    public JsonElement serialize(
            Contract contract,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Id_Contract",
                        contract.getIdContract());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "IssueDate",
                        contract.getIssueDate());

        //
        return jsonObject;
    }
}
