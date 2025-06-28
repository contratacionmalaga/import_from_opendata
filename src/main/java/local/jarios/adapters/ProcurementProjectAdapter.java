package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ProcurementProject;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(jsonObject,"Name",procurementProject.getName());

        JsonSerializationHelper
                .addProperty(jsonObject,"Description",procurementProject.getTypeCode());

        JsonSerializationHelper
                .addProperty(jsonObject,"SubtypeCode",procurementProject.getSubtypeCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,"MixContractIndicator",procurementProject.getMixContractIndicator());

        JsonSerializationHelper
                .addProperty(jsonObject,"Description",procurementProject.getDescription());

        //
        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "BudgetAmount",
                        procurementProject.getBudgetAmount(),
                        context);

        JsonSerializationHelper
                .addIfNotEmpty(
                        jsonObject,
                        "RequiredCommodityClassification",
                        procurementProject.getRequiredCommodityClassification(),
                        context);

        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "RealizedLocation",
                        procurementProject.getRealizedLocation(),
                        context);

        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "PlannedPeriod",
                        procurementProject.getPlannedPeriod(),
                        context);

        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "ContractExtension",
                        procurementProject.getContractExtension(),
                        context);

        //
        return jsonObject;
    }
}
