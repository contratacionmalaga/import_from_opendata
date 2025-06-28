package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.Contact;

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
        jsonObject.addProperty("Name", contact.getName());
        jsonObject.addProperty("Telephone", contact.getTelephone());
        jsonObject.addProperty("Telefax", contact.getTelefax());
        jsonObject.addProperty("ElectronicMail", contact.getElectronicMail());

        //
        return jsonObject;
    }
}
