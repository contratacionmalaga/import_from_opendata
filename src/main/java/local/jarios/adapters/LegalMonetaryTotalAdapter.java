package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.LegalMonetaryTotal;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record LegalMonetaryTotalAdapter() implements JsonSerializer<LegalMonetaryTotal> {

    @Override
    public JsonElement serialize(
            LegalMonetaryTotal legalMonetaryTotal,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("TaxExclusiveAmount", legalMonetaryTotal.getTaxExclusiveAmount());
        jsonObject.addProperty("TaxInclusiveAmount", legalMonetaryTotal.getTaxInclusiveAmount());
        jsonObject.addProperty("PayableAmount", legalMonetaryTotal.getPayableAmount());

        //
        return jsonObject;
    }
}
