package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AdditionalPublicationDocumentReference;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record AdditionalPublicationDocumentReferenceAdapter(boolean imprimirHijos)
        implements JsonSerializer<AdditionalPublicationDocumentReference> {

    @Override
    public JsonElement serialize(
            AdditionalPublicationDocumentReference additionalPublicationDocumentReference,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id",
                String.valueOf(additionalPublicationDocumentReference.getId()));
        jsonObject.addProperty("IssueDate",
                String.valueOf(additionalPublicationDocumentReference.getIssueDate()));
        jsonObject.addProperty("DocumentTypeCode",
                additionalPublicationDocumentReference.getDocumentTypeCode());

        if (imprimirHijos && additionalPublicationDocumentReference.getAttachment() != null) {

            //
            jsonObject.add(
                    "Attachment",
                    context.serialize(
                            additionalPublicationDocumentReference.getAttachment()));
        }

        return jsonObject;
    }
}
