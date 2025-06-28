package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Contract;

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

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id_Contract", String.valueOf(contract.getIdContract()));
        jsonObject.addProperty("IssueDate", String.valueOf(contract.getIssueDate()));

        return jsonObject;
    }
}
