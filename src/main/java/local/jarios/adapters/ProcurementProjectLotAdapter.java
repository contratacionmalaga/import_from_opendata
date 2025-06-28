package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ProcurementProjectLot;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ProcurementProjectLotAdapter(boolean imprimirHijos)
        implements JsonSerializer<ProcurementProjectLot> {

    @Override
    public JsonElement serialize(
            ProcurementProjectLot procurementProjectLot,
            Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("IdLote",
                procurementProjectLot.getIdLote());

        if (imprimirHijos) {

           //ProcurementProject es OBLIGATORIO para un ProcurementProjectLot
            jsonObject.add(
                    "ProcurementProject",
                    context.serialize(procurementProjectLot.getProcurementProject()));

            JsonSerializationHelper.addIfNotNull(
                    jsonObject,
                    "TenderingTerms",
                    procurementProjectLot.getTenderingTerms(),
                    context);

        }

        return jsonObject;
    }
}
