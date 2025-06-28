package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.Log;
import local.jarios.helpers.JsonSerializationHelper;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record LogAdapter(boolean imprimirHijos) implements JsonSerializer<Log> {

    @Override
    public JsonElement serialize(Log log, Type typeOfSrc, JsonSerializationContext context) {

        //
        JsonObject jsonObject = new JsonObject();

        // Crear un objeto para el contenido de "Log"
        JsonObject logContent = new JsonObject();

        //
        logContent.addProperty("LugarImportacion", String.valueOf(log.getLugarImportacion()));
        logContent.addProperty("TipoSindicacion", String.valueOf(log.getTipoSindicacion()));


        // Evitar serializar recursivamente las relaciones bidireccionales
        if (log.getEstadistica() != null) {
            logContent.add("Estadística", context.serialize(log.getEstadistica()));
        }

        //
        if (log.getConfiguracion() != null) {
            logContent.add("Configuración", context.serialize(log.getConfiguracion()));
        }

        //
        if (imprimirHijos) {

            JsonSerializationHelper.addIfNotEmpty(
                    jsonObject,
                    "List<Feed>",
                    log.getListFeed(),
                    context);
        }

        // Agregar el objeto "Log" que contendrá todos los datos anteriores
        jsonObject.add("Log", logContent);

        //
        return jsonObject;
    }
}
