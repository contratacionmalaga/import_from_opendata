package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TendererQualificationRequest;

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

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id",
                String.valueOf(tendererQualificationRequest.getId()));
        jsonObject.addProperty("PersonalSituation",
                tendererQualificationRequest.getPersonalSituation());
        jsonObject.addProperty("Description",
                tendererQualificationRequest.getDescription());

        if (imprimirHijos) {

            if (!tendererQualificationRequest.getEvaluationCriteria().isEmpty()) {
               //Serializar FinancialEvaluationCriteria
                jsonObject.add(
                        "EvaluationCriteria",
                        context.serialize(tendererQualificationRequest.getEvaluationCriteria()));
            }

            //
            if (!tendererQualificationRequest.getRequiredBusinessClassificationScheme().isEmpty()) {
               //Serializar RequiredBusinessClassificationScheme
                jsonObject.add(
                        "RequiredBusinessClassificationScheme",
                        context.serialize(tendererQualificationRequest.getRequiredBusinessClassificationScheme()));
            }

            //
            if (!tendererQualificationRequest.getSpecificTendererRequirement().isEmpty()) {
               //Serializar RequiredBusinessClassificationScheme
                jsonObject.add(
                        "SpecificTendererRequirement",
                        context.serialize(tendererQualificationRequest.getSpecificTendererRequirement()));
            }
        }

        return jsonObject;
    }
}
