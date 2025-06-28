package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ContractModification;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
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

        //
        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "LegalMonetaryTotal",
                contractModification.getContractModificationLegalMonetaryTotal(),
                context);

        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "FinalLegalMonetaryTotal",
                contractModification.getContractModificationFinalLegalMonetaryTotal(),
                context);

        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "FinalDurationMeasure",
                contractModification.getFinalDurationMeasure(),
                context);

        //
        return jsonObject;
    }
}
