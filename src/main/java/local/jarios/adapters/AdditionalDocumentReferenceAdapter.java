package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AdditionalDocumentReference;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record AdditionalDocumentReferenceAdapter(boolean imprimirHijos) implements JsonSerializer<AdditionalDocumentReference> {

    @Override
    public JsonElement serialize(
            AdditionalDocumentReference additionalDocumentReference,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("id", String.valueOf(additionalDocumentReference.getId()));

        //
        if (imprimirHijos) {

            //
            jsonObject.add(
                    "DocumentReference",
                    context.serialize(additionalDocumentReference.getDocumentReference()));
        }
        //
        return jsonObject;
    }
}
