package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ContractExecutionRequirement;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ContractExecutionRequirementAdapter() implements JsonSerializer<ContractExecutionRequirement> {

    @Override
    public JsonElement serialize(
            ContractExecutionRequirement contractExecutionRequirement,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("Name", contractExecutionRequirement.getName());
        jsonObject.addProperty("ExecutionRequirementCode", contractExecutionRequirement.getExecutionRequirementCode());
        jsonObject.addProperty("Description", contractExecutionRequirement.getDescription());

        //
        return jsonObject;
    }
}
