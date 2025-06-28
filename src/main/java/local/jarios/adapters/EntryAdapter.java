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
        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "IdEntry",
                entry.getIdEntry(),
                context);

        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "Link",
                entry.getLink(),
                context);

        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "Summary",
                entry.getSummary(),
                context);

        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "Title",
                entry.getTitle(),
                context);

        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "Updated",
                entry.getUpdated(),
                context);

        //
        JsonSerializationHelper.addIfNotEmpty(jsonObject, "List<ContractFolderStatus>", entry.getListContractFolderStatus(), context);

        //
        return jsonObject;
    }
}
