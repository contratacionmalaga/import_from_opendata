package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TenderingTerms;

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

        jsonObject.addProperty("Id",
                String.valueOf(tenderingTerms.getId()));
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

            if (tenderingTerms.getTendererQualificationRequest() != null)  {

                jsonObject.add(
                        "TendererQualificationRequest",
                        context.serialize(tenderingTerms.getTendererQualificationRequest()));
            }

            if (!tenderingTerms.getListContractExecutionRequirement().isEmpty()) {
                jsonObject.add(
                        "ContractExecutionRequirement",
                        context.serialize(tenderingTerms.getListContractExecutionRequirement()));
            }

            if (tenderingTerms.getAwardingTerms() != null)  {

                jsonObject.add(
                        "AwardingTerms",
                        context.serialize(tenderingTerms.getAwardingTerms()));
            }

            if (!tenderingTerms.getListFinancialGuarantee().isEmpty()) {
                jsonObject.add(
                        "FinancialGuarantee",
                        context.serialize(tenderingTerms.getListFinancialGuarantee()));
            }

            if (!tenderingTerms.getListAllowedSubcontractTerms().isEmpty()) {
                jsonObject.add(
                        "SubcontractTerms",
                        context.serialize(tenderingTerms.getListAllowedSubcontractTerms()));
            }

            if (tenderingTerms.getTenderRecipientParty() != null) {
                jsonObject.add(
                        "TenderRecipientParty",
                        context.serialize(tenderingTerms.getTenderRecipientParty()));
            }
        }

        return jsonObject;
    }
}
