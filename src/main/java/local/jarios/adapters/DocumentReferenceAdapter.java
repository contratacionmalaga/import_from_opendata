package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.DocumentReference;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonSerializationHelper.addProperty(
                jsonObject,
                "Id_DocumentReference",
                documentReference.getIdDocumentReference());

        //
        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "Attachment",
                documentReference.getAttachment(),
                context);

        //
        return jsonObject;
    }
}
