package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AdditionalPublicationStatus;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record AdditionalPublicationStatusAdapter(boolean imprimirHijos)
        implements JsonSerializer<AdditionalPublicationStatus> {

    @Override
    public JsonElement serialize(
            AdditionalPublicationStatus additionalPublicationStatus,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addProperty(
                jsonObject,
                "SendDateTime",
                additionalPublicationStatus.getPublicationMediaName());

        //
        JsonSerializationHelper.addIfNotEmpty(
                jsonObject,
                "List<AdditionalPublicationDocumentReference>",
                additionalPublicationStatus.getAdditionalPublicationDocumentReferenceList(),
                context);

        JsonSerializationHelper.addIfNotEmpty(
                jsonObject,
                "List<AdditionalPublicationRequest>",
                additionalPublicationStatus.getAdditionalPublicationRequestList(),
                context);

        //
        return jsonObject;
    }
}
