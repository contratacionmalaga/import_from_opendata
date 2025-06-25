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

        jsonObject.addProperty("id",
                String.valueOf(configuracion.getId()));
        jsonObject.addProperty("config_path",
                configuracion.getConfigPath());
        jsonObject.addProperty("config_fileName",
                configuracion.getConfigFileName());
        jsonObject.addProperty("config_url",
                configuracion.getConfigUrl());
        jsonObject.addProperty("config_filtros_fechaInicialLectura",
                configuracion.getConfigFiltroFechaInicialLectura());
        jsonObject.addProperty("config_filtros_fechaFinalLectura",
                configuracion.getConfigFiltroFechaFinalLectura());
        jsonObject.addProperty("config_filtros_filtroSql",
                configuracion.getConfigFiltroSql());
        jsonObject.addProperty("config_filtros_objeto",
                configuracion.getConfigFiltroObjeto());
        jsonObject.addProperty("config_filtros_nuts",
                configuracion.getConfigFiltroNuts());

        return jsonObject;
    }
}
