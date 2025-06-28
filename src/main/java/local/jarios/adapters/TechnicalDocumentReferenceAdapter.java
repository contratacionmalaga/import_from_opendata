package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TechnicalDocumentReference;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record TechnicalDocumentReferenceAdapter(boolean imprimirHijos) implements JsonSerializer<TechnicalDocumentReference> {

    @Override
    public JsonElement serialize(
            TechnicalDocumentReference technicalDocumentReference,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.add(
                "DocumentReference",
                context.serialize(technicalDocumentReference.getDocumentReference()));

        //
        return jsonObject;
    }
}
