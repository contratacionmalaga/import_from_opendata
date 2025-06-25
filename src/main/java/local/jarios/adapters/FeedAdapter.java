package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.atom.Feed;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record FeedAdapter(boolean imprimirHijos) implements JsonSerializer<Feed> {

    @Override
    public JsonElement serialize(Feed feed, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id", String.valueOf(feed.getId()));
        jsonObject.addProperty("LinkFirst", feed.getLinkFirst());
        jsonObject.addProperty("LinkPrev", feed.getLinkPrev());
        jsonObject.addProperty("LinkSelf", feed.getLinkSelf());
        jsonObject.addProperty("LinkNext", feed.getLinkNext());
        jsonObject.addProperty("Updated", String.valueOf(feed.getUpdated()));

        if (imprimirHijos && !feed.getListEntry().isEmpty()) {

            //
            jsonObject.add(
                    "Entrys",
                    context.serialize(feed.getListEntry()));
        }

        return jsonObject;
    }
}
