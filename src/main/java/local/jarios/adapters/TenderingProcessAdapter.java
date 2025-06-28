package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TenderingProcess;
import local.jarios.helpers.JsonSerializationHelper;

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
        jsonObject.addProperty("documentAvailabilityPeriod",
                String.valueOf(tenderingProcess.getDocumentAvailabilityPeriod()));
        jsonObject.addProperty("TenderSubmissionDeadlinePeriod",
                String.valueOf(tenderingProcess.getTenderSubmissionDeadlinePeriod()));

        //
        JsonSerializationHelper.addIfNotEmpty(jsonObject, "List<ProcessJustification>", tenderingProcess.getListProcessJustification(), context);

        JsonSerializationHelper.addIfNotNull(jsonObject, "EconomicOperatorShortList", tenderingProcess.getEconomicOperatorShortList(), context);
        JsonSerializationHelper.addIfNotNull(jsonObject, "AuctionTerms", tenderingProcess.getAuctionTerms(), context);


        //
        return jsonObject;
    }
}
