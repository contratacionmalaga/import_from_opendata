package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.auxiliares.Estadistica;

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

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("nFicheros", estadistica.getNFicheros());
        jsonObject.addProperty("nEntriesLeidos", estadistica.getNEntryLeidos());
        jsonObject.addProperty("nEntriesProcesados", estadistica.getNEntryProcesados());
        jsonObject.addProperty("nEntriesGrabados", estadistica.getNEntryGrabados());
        jsonObject.addProperty("nEntriesActualizados", estadistica.getNEntryActualizados());
        jsonObject.addProperty("nEntriesRechazados", estadistica.getNEntryRechazados());
        jsonObject.addProperty("duración", estadistica.getDuracion());

        return jsonObject;
    }
}

