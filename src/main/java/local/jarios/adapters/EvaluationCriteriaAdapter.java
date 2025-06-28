package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.EvaluationCriteria;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record EvaluationCriteriaAdapter() implements JsonSerializer<EvaluationCriteria> {

    @Override
    public JsonElement serialize(
            EvaluationCriteria evaluationCriteria,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "EvaluationCriteriaTypeCode",
                        evaluationCriteria.getEvaluationCriteriaTypeCode());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Description",
                        evaluationCriteria.getDescription());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "ThresholdQuantity",
                        evaluationCriteria.getThresholdQuantity());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "TipoSolvencia",
                        evaluationCriteria.getTipoSolvencia().toString());

        //
        return jsonObject;
    }
}
