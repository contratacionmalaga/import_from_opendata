package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ClassificationScheme;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Uuid",
                        classificationScheme.getUuid());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Name",
                        classificationScheme.getName());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Note",
                        classificationScheme.getNote());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Description",
                        classificationScheme.getDescription());

        //
        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "List<ClassificationCategory>",
                        classificationScheme.getClassificationCategory(),
                        context);

        //
        return jsonObject;
    }
}
