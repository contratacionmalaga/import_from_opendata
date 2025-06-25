package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ClassificationCategory;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ClassificationCategoryAdapter() implements JsonSerializer<ClassificationCategory> {

    @Override
    public JsonElement serialize(
            ClassificationCategory classificationCategory,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id",
                String.valueOf(classificationCategory.getId()));
        jsonObject.addProperty("CodeValue",
                String.valueOf(classificationCategory.getCodeValue()));

        return jsonObject;
    }
}
