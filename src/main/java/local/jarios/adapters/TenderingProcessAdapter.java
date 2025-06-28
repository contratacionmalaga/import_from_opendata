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

        //
        JsonObject jsonObject = new JsonObject();

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "ProcedureCode",
                        tenderingProcess.getProcedureCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "ContractingSystemCode",
                        tenderingProcess.getContractingSystemCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "UrgencyCode",
                        tenderingProcess.getUrgencyCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "SubmissionMethodCode",
                        tenderingProcess.getSubmissionMethodCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "PartPresentationCode",
                        tenderingProcess.getPartPresentationCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "MaximumLotPresentationQuantity",
                        tenderingProcess.getMaximumLotPresentationQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "MaximunTendererAwardedLotQuantity",
                        tenderingProcess.getMaximunTendererAwardedLotQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "LotsCombinationContractingAuthorityRights",
                        tenderingProcess.getLotsCombinationContractingAuthorityRights());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "OverThresholdIndicator",
                        tenderingProcess.getOverThresholdIndicator());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "RequirementTypeCode",
                        tenderingProcess.getDescription());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "DocumentAvailabilityPeriod",
                        tenderingProcess.getDocumentAvailabilityPeriod());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "TenderSubmissionDeadlinePeriod",
                        tenderingProcess.getTenderSubmissionDeadlinePeriod());

        //
        JsonSerializationHelper
                .addIfNotEmpty(
                        jsonObject,
                        "List<ProcessJustification>",
                        tenderingProcess.getListProcessJustification(),
                        context);

        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "EconomicOperatorShortList",
                        tenderingProcess.getEconomicOperatorShortList(),
                        context);

        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "AuctionTerms",
                        tenderingProcess.getAuctionTerms(),
                        context);

        //
        return jsonObject;
    }
}
