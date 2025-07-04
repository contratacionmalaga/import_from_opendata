package local.jarios.common.util;

import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Historico;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description: Variables globales del proyecto
 * Author: juan
 * Date: 16/03/2024
 * Team: Juan Antonio Ríos
 */
public final class VariablesGlobales {

    /** Almacena los idplataforma y órganos de contratación que se aplican mediante el filtro sql */
    @Getter
    @Setter
    private static Map<String, String> mapFiltro = new HashMap<>();

    /** Estructura utilizada para almacenar los Entry junto con su identificador duranet la ejecución del aplicativo */
    @Getter
    @Setter
    private static Map<String, Entry> mapBaseDatos = new HashMap<>();

    /** Conjunto de Feeds que se procesan durante una ejecución */
    @Getter
    @Setter
    private static Set<Feed> setFeeds = new HashSet<>();

    /** Lista que almacena las acciones que se realizan sobre cada Entry que se analiza */
    @Getter
    @Setter
    private static List<Historico> listHistoricos = new ArrayList<>();

    @Getter
    @Setter
    private static LocalDateTime filtroFechaInicial;

    @Getter
    @Setter
    private static LocalDateTime filtroFechaFinal;

    @Getter
    @Setter
    private static String filtroObjeto;

    @Getter
    @Setter
    private static HashSet<String> filtroNuts;

    @Getter
    @Setter
    private static String filtroSql;

    private VariablesGlobales() { }
}
