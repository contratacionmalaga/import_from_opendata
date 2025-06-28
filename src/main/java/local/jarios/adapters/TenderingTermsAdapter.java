package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TenderingTerms;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record TenderingTermsAdapter(boolean imprimirHijos) implements JsonSerializer<TenderingTerms> {

    @Override
    public JsonElement serialize(
            TenderingTerms tenderingTerms, Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "RequiredCurriculaIndicator",
                        tenderingTerms.getReceivedAppealQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "VariantConstraintIndicator",
                        tenderingTerms.getVariantConstraintIndicator());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "PriceRevisionFormulaDescription",
                        tenderingTerms.getPriceRevisionFormulaDescription());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "FundingProgramCode",
                        tenderingTerms.getFundingProgramCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "FundingProgram",
                        tenderingTerms.getFundingProgram());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "ProcurementNationalLegislationCode",
                        tenderingTerms.getProcurementNationalLegislationCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "ProcurementLegislationDocumentReference",
                        tenderingTerms.getProcurementLegislationDocumentReference());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "ReceivedAppealQuantity",
                        tenderingTerms.getReceivedAppealQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "EorderingIndicator",
                        tenderingTerms.getEorderingIndicator());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "EpaymentMeansIndicator",
                        tenderingTerms.getEpaymentMeansIndicator());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "ElectronicInvoicingIndicator",
                        tenderingTerms.getElectronicInvoicingIndicator());

        if (imprimirHijos) {

            JsonSerializationHelper
                    .addIfNotNull(
                            jsonObject,
                            "TendererQualificationRequest",
                            tenderingTerms.getTendererQualificationRequest(),
                            context);

            JsonSerializationHelper
                    .addIfNotEmpty(
                            jsonObject,
                            "List<ContractExecutionRequirement>",
                            tenderingTerms.getListContractExecutionRequirement(),
                            context);

            JsonSerializationHelper
                    .addIfNotNull(
                            jsonObject,
                            "AwardingTerms",
                            tenderingTerms.getAwardingTerms(),
                            context);

            JsonSerializationHelper
                    .addIfNotEmpty(
                            jsonObject,
                            "List<FinancialGuarantee>",
                            tenderingTerms.getListFinancialGuarantee(),
                            context);

            JsonSerializationHelper
                    .addIfNotEmpty(
                            jsonObject,
                            "List<SubcontractTerms>",
                            tenderingTerms.getListAllowedSubcontractTerms(),
                            context);

            JsonSerializationHelper
                    .addIfNotNull(
                            jsonObject,
                            "TenderRecipientParty",
                            tenderingTerms.getTenderRecipientParty(),
                            context);

        }

        return jsonObject;
    }
}
