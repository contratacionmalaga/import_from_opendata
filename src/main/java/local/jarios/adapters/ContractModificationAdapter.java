package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ContractModification;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ContractModificationAdapter(boolean imprimirHijos) implements JsonSerializer<ContractModification> {

    @Override
    public JsonElement serialize(
            ContractModification contractModification,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id",
                String.valueOf(contractModification.getId()));
        jsonObject.addProperty("ContractId",
                contractModification.getContractId());
        jsonObject.addProperty("IdModificacion",
                contractModification.getIdContractModification());
        jsonObject.addProperty("IssueDate",
                String.valueOf(contractModification.getIssueDate()));
        jsonObject.addProperty("Note",
                contractModification.getNote());
        jsonObject.addProperty("ContractModificationLotId",
                contractModification.getContractModificationLotId());

        if (imprimirHijos) {

            //
            jsonObject.add(
                    "LegalMonetaryTotal",
                    context.serialize(contractModification.getContractModificationLegalMonetaryTotal()));

            //
            jsonObject.add(
                    "FinalLegalMonetaryTotal",
                    context.serialize(contractModification.getContractModificationFinalLegalMonetaryTotal()));

            //
            jsonObject.add(
                    "FinalDurationMeasure",
                    context.serialize(contractModification.getFinalDurationMeasure()));
        }

        return jsonObject;
    }
}
