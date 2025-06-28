package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.AdditionalPublicationStatus;

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

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("PublicationMediaName",
                additionalPublicationStatus.getPublicationMediaName());

        if (imprimirHijos) {
            //
            if (!additionalPublicationStatus.getAdditionalPublicationRequestList().isEmpty()) {
                //
                jsonObject.add(
                        "AdditionalPublicationRequest",
                        context.serialize(
                                additionalPublicationStatus.getAdditionalPublicationRequestList()));
            }

            if (!additionalPublicationStatus.getAdditionalPublicationDocumentReferenceList().isEmpty()) {
                //
                jsonObject.add(
                        "AdditionalPublicationDocumentReference",
                        context.serialize(
                                additionalPublicationStatus
                                        .getAdditionalPublicationDocumentReferenceList()));
            }

        }

        return jsonObject;
    }
}
