package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TenderResult;

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

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id",
                String.valueOf(tenderResult.getId()));
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
        jsonObject.addProperty("StartDate",
                tenderResult.getStartDate());
        jsonObject.addProperty("AwardedOwnerNationalityCode",
                tenderResult.getAwardedOwnerNationalityCode());

        if (imprimirHijos) {

            //
            if (tenderResult.getContract() != null) {

                // Serializar Contract
                jsonObject.add(
                        "Contract",
                        context.serialize(tenderResult.getContract()));
            }

            //
            if (tenderResult.getWinningParty() != null) {

                // Serializar WinningParty
                jsonObject.add(
                        "WinningParty",
                        context.serialize(tenderResult.getWinningParty()));
            }


            // AwardedTenderedProject
            if (tenderResult.getAwardedTenderedProject() != null) {
                jsonObject.add(
                        "AwardedTenderedProject",
                        context.serialize(tenderResult.getAwardedTenderedProject()));
            }

            //
            if (tenderResult.getContract() != null) {

                // Serializar AwardedTenderedProject
                jsonObject.add(
                        "AwardedTenderedProject",
                        context.serialize(tenderResult.getAwardedTenderedProject()));
            }

            //
            if (!tenderResult.getListSubcontractTerms().isEmpty()) {

                // Serializar SubcontractTerms
                jsonObject.add(
                        "SubcontractTerms",
                        context.serialize(tenderResult.getListSubcontractTerms()));
            }
        }

        //
        return jsonObject;
    }
}
