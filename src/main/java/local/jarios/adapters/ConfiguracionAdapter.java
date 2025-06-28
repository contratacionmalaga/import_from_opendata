package local.jarios.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.helpers.JsonSerializationHelper;

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

        //
        JsonObject jsonObject = new JsonObject();

        //
        JsonSerializationHelper.addProperty(
                jsonObject,
                "Path",
                configuracion.getPath());
        JsonSerializationHelper.addProperty(
                jsonObject,
                "Filename",
                configuracion.getFilename());
        JsonSerializationHelper.addProperty(
                jsonObject,
                "Url",
                configuracion.getUrl());
        JsonSerializationHelper.addProperty(
                jsonObject,
                "FiltroFechaInicioLectura",
                configuracion.getFiltroFechaInicioLectura());
        JsonSerializationHelper.addProperty(
                jsonObject,
                "FiltroFechaFinLectura",
                configuracion.getFiltroFechaFinLectura());
        JsonSerializationHelper.addProperty(
                jsonObject,
                "FiltroSql",
                configuracion.getFiltroSql());
        JsonSerializationHelper.addProperty(
                jsonObject,
                "FiltroObjeto",
                configuracion.getFiltroObjeto());
        JsonSerializationHelper.addProperty(
                jsonObject,
                "FiltroNuts",
                configuracion.getFiltroNuts());

        //
        return jsonObject;
    }
}
