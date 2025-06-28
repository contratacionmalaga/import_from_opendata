package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.FinancialGuarantee;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record FinancialGuaranteeAdapter() implements JsonSerializer<FinancialGuarantee> {

    @Override
    public JsonElement serialize(
            FinancialGuarantee financialGuarantee,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("GuaranteeTypeCode", financialGuarantee.getGuaranteeTypeCode());
        jsonObject.addProperty("AmountRate", financialGuarantee.getAmountRate());
        jsonObject.addProperty("LiabilityAmount", financialGuarantee.getLiabilityAmount());

        return jsonObject;
    }
}
