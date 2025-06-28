package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TendererQualificationRequest;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record TendererQualificationRequestAdapter(boolean imprimirHijos)
        implements JsonSerializer<TendererQualificationRequest> {

    @Override
    public JsonElement serialize(
            TendererQualificationRequest tendererQualificationRequest,
            Type typeOfSrc,
            JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("PersonalSituation", tendererQualificationRequest.getPersonalSituation());
        jsonObject.addProperty("Description", tendererQualificationRequest.getDescription());

        //
        JsonSerializationHelper.addIfNotEmpty(jsonObject, "List<EvaluationCriteria>", tendererQualificationRequest.getEvaluationCriteria(), context);
        JsonSerializationHelper.addIfNotEmpty(jsonObject, "List<RequiredBusinessClassificationScheme>", tendererQualificationRequest.getRequiredBusinessClassificationScheme(), context);
        JsonSerializationHelper.addIfNotEmpty(jsonObject, "List<SpecificTendererRequirement>", tendererQualificationRequest.getSpecificTendererRequirement(), context);

        //
        return jsonObject;
    }
}

