package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.GeneralDocumentDocumentReference;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record GeneralDocumentDocumentReferenceAdapter() implements JsonSerializer<GeneralDocumentDocumentReference> {

    @Override
    public JsonElement serialize(
            GeneralDocumentDocumentReference generalDocumentDocumentReference,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty(
                "Id",
                String.valueOf(generalDocumentDocumentReference.getId()));

        if (generalDocumentDocumentReference.getDocumentReference() != null) {
            jsonObject.add(
                    "DocumentReference",
                    context.serialize(generalDocumentDocumentReference.getDocumentReference()));
        }
        return jsonObject;
    }
}
