package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.NoticeInfo;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
        jsonObject.addProperty("NoticeTypeCode", noticeInfo.getNoticeTypeCode());

        //
        JsonSerializationHelper.addIfNotEmpty(jsonObject, "List<AdditionalPublicationStatus>", noticeInfo.getListAdditionalPublicationStatus(), context);

        //
        return jsonObject;
    }
}
