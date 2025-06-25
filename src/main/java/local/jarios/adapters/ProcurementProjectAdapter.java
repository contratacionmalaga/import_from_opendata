package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ProcurementProject;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ProcurementProjectAdapter(boolean imprimirHijos)
        implements JsonSerializer<ProcurementProject> {

    @Override
    public JsonElement serialize(
            ProcurementProject procurementProject,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id",
                String.valueOf(procurementProject.getId()));
        jsonObject.addProperty("Name",
                procurementProject.getName());
        jsonObject.addProperty("Description",
                procurementProject.getDescription());
        jsonObject.addProperty("TypeCode",
                procurementProject.getTypeCode());
        jsonObject.addProperty("SubtypeCode",
                procurementProject.getSubtypeCode());
        jsonObject.addProperty("MixContractIndicator",
                procurementProject.getMixContractIndicator());

        if (imprimirHijos) {

            if (procurementProject.getBudgetAmount() != null) {

                // BudgetAmount
                jsonObject.add(
                        "BudgetAmount",
                        context.serialize(procurementProject.getBudgetAmount()));
            }

            if (procurementProject.getRealizedLocation() != null) {

                // RealizedLocation
                jsonObject.add(
                        "RealizedLocation",
                        context.serialize(procurementProject.getRealizedLocation()));
            }

            if (!procurementProject.getRequiredCommodityClassification().isEmpty()) {

                // CommodityClassification
                jsonObject.add(
                        "CommodityClassification",
                        context.serialize(procurementProject.getRequiredCommodityClassification()));
            }

            if (procurementProject.getPlannedPeriod() != null) {

                // PlannedPeriod
                jsonObject.add(
                        "PlannedPeriod",
                        context.serialize(procurementProject.getPlannedPeriod()));
            }

            if (procurementProject.getContractExtension() != null) {

                // ContractExtension
                jsonObject.add(
                        "ContractExtension",
                        context.serialize(procurementProject.getContractExtension()));
            }
        }

        return jsonObject;
    }
}
