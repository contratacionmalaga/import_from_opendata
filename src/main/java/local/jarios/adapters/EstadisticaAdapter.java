package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record EstadisticaAdapter() implements JsonSerializer<Estadistica> {

    @Override
    public JsonElement serialize(
            Estadistica estadistica, Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "nRegistrosHistoricosInsertar",
                        estadistica.getNRegistrosHistoricosInsertar());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "nRegistrosHistoricosEliminar",
                        estadistica.getNRegistrosHistoricosEliminar());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "nRegistrosHistoricosActualizar",
                        estadistica.getNRegistrosHistoricosActualizar());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "nRegistrosHistoricosRechazar",
                        estadistica.getNRegistrosHistoricosRechazar());

        JsonSerializationHelper
                .addProperty(
                        jsonObject,
                        "Duracióm",
                        estadistica.getDuracion());

        //
        return jsonObject;
    }
}

