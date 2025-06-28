package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TenderResult;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record TenderResultAdapter(boolean imprimirHijos) implements JsonSerializer<TenderResult> {

    @Override
    public JsonElement serialize(
            TenderResult tenderResult, Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("ResultCode",
                tenderResult.getResultCode());
        jsonObject.addProperty("ReceivedTenderQuantity",
                tenderResult.getReceivedTenderQuantity());
        jsonObject.addProperty("Description",
                tenderResult.getDescription());
        jsonObject.addProperty("AwardDate",
                String.valueOf(tenderResult.getAwardDate()));
        jsonObject.addProperty("LowerTenderAmountQuantity",
                tenderResult.getLowerTenderAmountQuantity());
        jsonObject.addProperty("HigherTenderAmountQuantity",
                tenderResult.getHigherTenderAmountQuantity());
        jsonObject.addProperty("AbnormallyLowTendersIndicator",
                tenderResult.getAbnormallyLowTendersIndicator());
        jsonObject.addProperty("SMEsReceivedTenderQuantity",
                tenderResult.getSMEsReceivedTenderQuantity());
        jsonObject.addProperty("SMEAwardedIndicator",
                tenderResult.getSMEAwardedIndicator());
        jsonObject.addProperty("EUNationalsReceivedTenderQuantity",
                tenderResult.getEUNationalsReceivedTenderQuantity());
        jsonObject.addProperty("NonEUNationalsReceivedTenderQuantity", 
                tenderResult.getNonEUNationalsReceivedTenderQuantity());
        jsonObject.addProperty("StartDate", String.valueOf(tenderResult.getStartDate()));
        jsonObject.addProperty("AwardedOwnerNationalityCode", tenderResult.getAwardedOwnerNationalityCode());

        //
        if (imprimirHijos) {

            JsonSerializationHelper.addIfNotNull(jsonObject, "Contract", tenderResult.getContract(), context);
            JsonSerializationHelper.addIfNotNull(jsonObject, "WinningParty", tenderResult.getWinningParty(), context);
            JsonSerializationHelper.addIfNotNull(jsonObject, "AwardedTenderedProject", tenderResult.getAwardedTenderedProject(), context);
            JsonSerializationHelper.addIfNotNull(jsonObject, "AwardedTenderedProject", tenderResult.getAwardedTenderedProject(), context);
            JsonSerializationHelper.addIfNotEmpty(jsonObject, "List<SubcontractTerms>", tenderResult.getListSubcontractTerms(), context);

        }

        //
        return jsonObject;
    }
}
