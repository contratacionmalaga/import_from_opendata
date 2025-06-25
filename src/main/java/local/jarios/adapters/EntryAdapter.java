package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.atom.Entry;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record EntryAdapter(boolean imprimirHijos) implements JsonSerializer<Entry> {

    @Override
    public JsonElement serialize(Entry entry, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id", String.valueOf(entry.getId()));
        jsonObject.addProperty("IdEntry", String.valueOf(entry.getIdEntry()));
        jsonObject.addProperty("Link", entry.getLink());
        jsonObject.addProperty("Summary", entry.getSummary());
        jsonObject.addProperty("Title", entry.getTitle());
        jsonObject.addProperty("Updated", String.valueOf(entry.getUpdated()));

        if (imprimirHijos && !entry.getListContractFolderStatus().isEmpty()) {

            //
            jsonObject.add(
                    "ContractFolderStatus",
                    context.serialize(entry.getListContractFolderStatus()));
        }

        return jsonObject;
    }
}
