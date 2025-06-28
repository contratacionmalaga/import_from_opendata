package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ExternalReference;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ExternalReferenceAdapter() implements JsonSerializer<ExternalReference> {

    @Override
    public JsonElement serialize(
            ExternalReference externalReference,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("Id", String.valueOf(externalReference.getId()));
        jsonObject.addProperty("Uri", String.valueOf(externalReference.getUri()));
        jsonObject.addProperty("DocumentHash", externalReference.getDocumentHash());
        jsonObject.addProperty("Filename", externalReference.getFilename());

        //
        return jsonObject;
    }
}
