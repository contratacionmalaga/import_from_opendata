package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Contact;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ContactAdapter() implements JsonSerializer<Contact> {

    @Override
    public JsonElement serialize(
            Contact contact,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addProperty(
                jsonObject,
                "Name",
                contact.getName());

        JsonSerializationHelper.addProperty(
                jsonObject,
                "Telephone",
                contact.getTelephone());

        JsonSerializationHelper.addProperty(
                jsonObject,
                "Telefax",
                contact.getTelefax());

        JsonSerializationHelper.addProperty(
                jsonObject,
                "ElectronicMail",
                contact.getElectronicMail());

        //
        return jsonObject;
    }
}
