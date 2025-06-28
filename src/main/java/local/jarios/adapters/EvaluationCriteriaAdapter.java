package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.EvaluationCriteria;

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
        jsonObject.addProperty("EvaluationCriteriaTypeCode", evaluationCriteria.getEvaluationCriteriaTypeCode());
        jsonObject.addProperty("Description", evaluationCriteria.getDescription());
        jsonObject.addProperty("ThresholdQuantity", evaluationCriteria.getThresholdQuantity());
        jsonObject.addProperty("TipoSolvencia", evaluationCriteria.getTipoSolvencia().toString());

        //
        return jsonObject;
    }
}
