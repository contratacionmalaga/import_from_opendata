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

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("RequiredCurriculaIndicator",
                tenderingTerms.getRequiredCurriculaIndicator());
        jsonObject.addProperty("VariantConstraintIndicator",
                tenderingTerms.getVariantConstraintIndicator());
        jsonObject.addProperty("PriceRevisionFormulaDescription",
                tenderingTerms.getPriceRevisionFormulaDescription());
        jsonObject.addProperty("FundingProgramCode",
                tenderingTerms.getFundingProgramCode());
        jsonObject.addProperty("FundingProgram",
                tenderingTerms.getFundingProgram());
        jsonObject.addProperty("ProcurementNationalLegislationCode",
                tenderingTerms.getProcurementNationalLegislationCode());
        jsonObject.addProperty("ProcurementLegislationDocumentReference",
                tenderingTerms.getProcurementLegislationDocumentReference());
        jsonObject.addProperty("ReceivedAppealQuantity",
                tenderingTerms.getReceivedAppealQuantity());
        jsonObject.addProperty("EorderingIndicator",
                tenderingTerms.getEorderingIndicator());
        jsonObject.addProperty("EpaymentMeansIndicator",
                tenderingTerms.getEpaymentMeansIndicator());
        jsonObject.addProperty("ElectronicInvoicingIndicator",
                tenderingTerms.getElectronicInvoicingIndicator());

        if (imprimirHijos) {

            JsonSerializationHelper.addIfNotNull(jsonObject, "TendererQualificationRequest", tenderingTerms.getTendererQualificationRequest(), context);
            JsonSerializationHelper.addIfNotEmpty(jsonObject, "ContractExecutionRequirement", tenderingTerms.getListContractExecutionRequirement(), context);
            JsonSerializationHelper.addIfNotNull(jsonObject, "AwardingTerms", tenderingTerms.getAwardingTerms(), context);
            JsonSerializationHelper.addIfNotEmpty(jsonObject, "FinancialGuarantee", tenderingTerms.getListFinancialGuarantee(), context);
            JsonSerializationHelper.addIfNotEmpty(jsonObject, "SubcontractTerms", tenderingTerms.getListAllowedSubcontractTerms(), context);
            JsonSerializationHelper.addIfNotNull(jsonObject, "TenderRecipientParty", tenderingTerms.getTenderRecipientParty(), context);

        }

        return jsonObject;
    }
}
