package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.FinancialGuarantee;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addProperty(
                jsonObject,
                "GuaranteeTypeCode",
                financialGuarantee.getGuaranteeTypeCode());

        JsonSerializationHelper.addProperty(
                jsonObject,
                "AmountRate",
                financialGuarantee.getAmountRate());

        JsonSerializationHelper.addProperty(
                jsonObject,
                "LiabilityAmount",
                financialGuarantee.getLiabilityAmount());

        //
        return jsonObject;
    }
}
