package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.BudgetAmount;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record BudgetAmountAdapter() implements JsonSerializer<BudgetAmount> {

    @Override
    public JsonElement serialize(
            BudgetAmount budgetAmount,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("EstimatedOverallContratAmount", budgetAmount.getEstimatedOverallContractAmount());
        jsonObject.addProperty("TotalAmount", budgetAmount.getTotalAmount());
        jsonObject.addProperty("TaxExclusiveAmount", budgetAmount.getTaxExclusiveAmount());

        //
        return jsonObject;
    }
}
