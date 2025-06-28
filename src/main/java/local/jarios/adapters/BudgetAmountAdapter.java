package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.BudgetAmount;
import local.jarios.helpers.JsonSerializationHelper;

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
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "EstimatedOverallContratAmount",
                        budgetAmount.getEstimatedOverallContractAmount());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "TotalAmount",
                        budgetAmount.getTotalAmount());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "TaxExclusiveAmount",
                        budgetAmount.getTaxExclusiveAmount());

        //
        return jsonObject;
    }
}
