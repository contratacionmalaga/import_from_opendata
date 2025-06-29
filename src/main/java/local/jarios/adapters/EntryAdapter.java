package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.atom.Entry;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "IdEntry",
                        entry.getIdEntry());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Link",
                        entry.getLink());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Summary",
                        entry.getSummary());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Title",
                        entry.getTitle());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Updated",
                        entry.getUpdated());

        if (imprimirHijos) {
            //
            JsonSerializationHelper
                    .addIfNotEmpty(
                            jsonObject,
                            "List<ContractFolderStatus>",
                            entry.getListContractFolderStatus(),
                            context);
        }

        //
        return jsonObject;
    }
}
