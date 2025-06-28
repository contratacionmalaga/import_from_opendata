package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.GeneralDocument;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "GeneralDocumentDocumentReference",
                        generalDocument.getGeneralDocumentDocumentReference(),
                        context);

        //
        return jsonObject;
    }
}
