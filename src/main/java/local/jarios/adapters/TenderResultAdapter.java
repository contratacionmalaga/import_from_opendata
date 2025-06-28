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
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "ResultCode",
                        tenderResult.getResultCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "ReceivedTenderQuantity",
                        tenderResult.getReceivedTenderQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Description",
                        tenderResult.getDescription());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "AwardDate",
                        tenderResult.getAwardDate());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "LowerTenderAmountQuantity",
                        tenderResult.getLowerTenderAmountQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "HigherTenderAmountQuantity",
                        tenderResult.getHigherTenderAmountQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "AbnormallyLowTendersIndicator",
                        tenderResult.getAbnormallyLowTendersIndicator());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "SMEsReceivedTenderQuantity",
                        tenderResult.getSMEsReceivedTenderQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "SMEAwardedIndicator",
                        tenderResult.getSMEAwardedIndicator());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "EUNationalsReceivedTenderQuantity",
                        tenderResult.getEUNationalsReceivedTenderQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "NonEUNationalsReceivedTenderQuantity",
                        tenderResult.getNonEUNationalsReceivedTenderQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "StartDate",
                        tenderResult.getStartDate());

        jsonObject
                .addProperty(
                        "AwardedOwnerNationalityCode",
                        tenderResult.getAwardedOwnerNationalityCode());

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
