package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.NoticeInfo;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record NoticeInfoAdapter(boolean imprimirHijos) implements JsonSerializer<NoticeInfo> {

    @Override
    public JsonElement serialize(
            NoticeInfo noticeInfo, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Id", String.valueOf(noticeInfo.getId()));
        jsonObject.addProperty("NoticeTypeCode", noticeInfo.getNoticeTypeCode());

        if (imprimirHijos && !noticeInfo.getListAdditionalPublicationStatus().isEmpty()) {

            jsonObject.add(
                    "AdditionalPublicationStatus",
                    context.serialize(noticeInfo.getListAdditionalPublicationStatus()));
        }

        return jsonObject;
    }
}
