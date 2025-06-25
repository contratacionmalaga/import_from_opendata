package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.placsp.ProcurementProjectLot;

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

        jsonObject.addProperty("Id",
                String.valueOf(procurementProjectLot.getId()));
        jsonObject.addProperty("IdLote",
                procurementProjectLot.getIdLote());

        if (imprimirHijos) {

           //ProcurementProject es OBLIGATORIO para un ProcurementProjectLot
            jsonObject.add(
                    "ProcurementProject",
                    context.serialize(procurementProjectLot.getProcurementProject()));

            if (procurementProjectLot.getTenderingTerms() != null) {
                jsonObject.add(
                        "TenderingTerms",
                        context.serialize(procurementProjectLot.getTenderingTerms()));
            }
        }

        return jsonObject;
    }
}
