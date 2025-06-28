package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.LegalDocumentReference;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record LegalDocumentReferenceAdapter(boolean imprimirHijos) implements JsonSerializer<LegalDocumentReference> {

    @Override
    public JsonElement serialize(
            LegalDocumentReference legalDocumentReference,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.add(
                "DocumentReference",
                context.serialize(legalDocumentReference.getDocumentReference()));

        //
        return jsonObject;
    }
}
