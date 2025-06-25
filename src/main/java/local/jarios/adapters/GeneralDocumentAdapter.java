package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.GeneralDocument;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record GeneralDocumentAdapter() implements JsonSerializer<GeneralDocument> {

    @Override
    public JsonElement serialize(
            GeneralDocument generalDocument,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty(
                "Id",
                String.valueOf(generalDocument.getId()));

        if (generalDocument.getGeneralDocumentDocumentReference() != null) {
            jsonObject.add(
                    "GeneralDocumentDocumentReference",
                    context.serialize(generalDocument.getGeneralDocumentDocumentReference()));
        }
        return jsonObject;
    }
}
