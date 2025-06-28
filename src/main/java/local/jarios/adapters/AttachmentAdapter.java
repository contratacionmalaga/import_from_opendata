package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Attachment;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record AttachmentAdapter(boolean imprimirHijos) implements JsonSerializer<Attachment> {

    @Override
    public JsonElement serialize(
            Attachment attachment,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addIfNotNull(jsonObject, "ExternalReference", attachment.getExternalReference(), context);

        //
        return jsonObject;
    }
}
