package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.TenderedProject;

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

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ProcurementProjectLotId", awardedTenderedProject.getProcurementProjectLotId());

        if ((imprimirHijos) && (awardedTenderedProject.getLegalMonetaryTotal() != null)) {

            // Serializar Contract
            jsonObject.add(
                    "LegalMonetaryTotal",
                    context.serialize(awardedTenderedProject.getLegalMonetaryTotal()));
        }

        //
        return jsonObject;
    }
}
