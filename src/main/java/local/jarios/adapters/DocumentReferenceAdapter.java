package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.DocumentReference;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record DocumentReferenceAdapter(boolean imprimirHijos) implements JsonSerializer<DocumentReference> {

    @Override
    public JsonElement serialize(
            DocumentReference documentReference,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id_documentreference", documentReference.getIdDocumentReference());

        // Attachment
        if (imprimirHijos && documentReference.getAttachment() != null) {
            jsonObject.add(
                    "Attachment",
                    context.serialize(documentReference.getAttachment()));
        }

        return jsonObject;
    }
}
