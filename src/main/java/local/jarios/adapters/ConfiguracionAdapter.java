package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.auxiliares.Configuracion;

import java.lang.reflect.Type;

/**
 * Description: Juan
 * Author: juan
 * Date: 09/07/2024
 * Team: Juan
 */
public record ConfiguracionAdapter() implements JsonSerializer<Configuracion> {

    @Override
    public JsonElement serialize(
            Configuracion configuracion, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("path", configuracion.getPath());
        jsonObject.addProperty("filemame", configuracion.getFilename());
        jsonObject.addProperty("url", configuracion.getUrl());
        jsonObject.addProperty("filtroFechaInicialLectura", configuracion.getFiltroFechaInicioLectura());
        jsonObject.addProperty("filtroFechaFinalLectura", configuracion.getFiltroFechaFinLectura());
        jsonObject.addProperty("filtroSql", configuracion.getFiltroSql());
        jsonObject.addProperty("filtroObjeto", configuracion.getFiltroObjeto());
        jsonObject.addProperty("filtroNuts", configuracion.getFiltroNuts());

        return jsonObject;
    }
}
