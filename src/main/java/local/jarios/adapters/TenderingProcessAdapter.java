package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TenderingProcess;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record TenderingProcessAdapter(boolean imprimirHijos) implements JsonSerializer<TenderingProcess> {

    @Override
    public JsonElement serialize(
            TenderingProcess tenderingProcess, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id",
                String.valueOf(tenderingProcess.getId()));
        jsonObject.addProperty("ProcedureCode",
                tenderingProcess.getProcedureCode());
        jsonObject.addProperty("ContractingSystemCode",
                tenderingProcess.getContractingSystemCode());
        jsonObject.addProperty("UrgencyCode",
                tenderingProcess.getUrgencyCode());
        jsonObject.addProperty("SubmissionMethodCode",
                tenderingProcess.getSubmissionMethodCode());
        jsonObject.addProperty("PartPresentationCode",
                tenderingProcess.getPartPresentationCode());
        jsonObject.addProperty("MaximumLotPresentationQuantity",
                tenderingProcess.getMaximumLotPresentationQuantity());
        jsonObject.addProperty("MaximunTendererAwardedLotQuantity",
                tenderingProcess.getMaximunTendererAwardedLotQuantity());
        jsonObject.addProperty("LotsCombinationContractingAuthorityRights",
                tenderingProcess.getLotsCombinationContractingAuthorityRights());
        jsonObject.addProperty("OverThresholdIndicator",
                tenderingProcess.getOverThresholdIndicator());
        jsonObject.addProperty("Description",
                tenderingProcess.getDescription());

        if (imprimirHijos) {

            // ProcessJustification
            jsonObject.add(
                    "ProcessJustification",
                    context.serialize(tenderingProcess.getListProcessJustification()));

            // EconomicOperatorShortList
            if ((tenderingProcess.getEconomicOperatorShortList() != null)) {

                //
                jsonObject.add(
                    "EconomicOperatorShortList",
                    context.serialize(tenderingProcess.getEconomicOperatorShortList()));
            }

            // AuctionTerms
            if ((tenderingProcess.getAuctionTerms() != null)) {

                //
                jsonObject.add(
                    "AuctionTerms",
                    context.serialize(tenderingProcess.getAuctionTerms()));
            }

            // TenderSubmissionDeadlinePeriod
            if ((tenderingProcess.getTenderSubmissionDeadlinePeriod() != null)) {

                //
                jsonObject.add(
                    "TenderSubmissionDeadlinePeriod",
                    context.serialize(tenderingProcess.getTenderSubmissionDeadlinePeriod()));
            }
        }

        //
        return jsonObject;
    }
}
