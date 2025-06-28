package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ClassificationScheme;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ClassificationSchemeAdapter(boolean imprimirHijos)
        implements JsonSerializer<ClassificationScheme> {

    @Override
    public JsonElement serialize(
            ClassificationScheme classificationScheme,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        if (imprimirHijos && !classificationScheme.getClassificationCategory().isEmpty()) {
            jsonObject.add(
                    "ClassificationCategory",
                    context.serialize(classificationScheme.getClassificationCategory()));
        }

        return jsonObject;
    }
}
