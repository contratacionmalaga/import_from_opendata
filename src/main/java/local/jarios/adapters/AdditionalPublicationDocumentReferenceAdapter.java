package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AdditionalPublicationDocumentReference;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "IssueDate",
                        additionalPublicationDocumentReference.getIssueDate());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "DocumentTypeCode",
                        additionalPublicationDocumentReference.getDocumentTypeCode());

        //
        JsonSerializationHelper
                .addIfNotNull(
                        jsonObject,
                        "Attachment",
                        additionalPublicationDocumentReference.getAttachment(),
                        context);

        //
        return jsonObject;
    }
}
