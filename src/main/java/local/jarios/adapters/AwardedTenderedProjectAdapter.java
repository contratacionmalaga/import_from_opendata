package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TenderedProject;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record AwardedTenderedProjectAdapter(boolean imprimirHijos) implements JsonSerializer<TenderedProject> {

    @Override
    public JsonElement serialize(
            TenderedProject awardedTenderedProject,
            Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addProperty(
                jsonObject,
                "ProcurementProjectLotId",
                awardedTenderedProject.getProcurementProjectLotId());

        //
        JsonSerializationHelper.addIfNotNull(
                jsonObject,
                "LegalMonetaryTotal",
                awardedTenderedProject.getLegalMonetaryTotal(),
                context);

        //
        return jsonObject;
    }
}
