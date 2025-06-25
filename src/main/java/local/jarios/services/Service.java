package local.jarios.services;

import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiServiceException;
import local.jarios.models.FiltroOrganoContratacion;

import java.util.List;
import java.util.Map;

/**
 * Interfaz que define los métodos principales para la persistencia y recuperación
 * de información relacionada con la importación de datos abiertos.
 */
public interface Service {

    /**
     * Persiste un objeto de log en el sistema.
     *
     * @param miLog el objeto de log que se desea almacenar.
     * @param mapBaseDatos       el mapa que representa la base de datos con claves como IDs o nombres.
     * @param lugarImportacion   información sobre el origen o contexto de la importación.
     */
    void persistirLog(Log miLog, Map<String, Entry> mapBaseDatos, LugarImportacion lugarImportacion) throws MiServiceException;


    /**
     * Obtiene el feed más reciente correspondiente a un tipo específico de sindicación.
     *
     * @param tipoSindicacion el tipo de sindicación (RSS, Atom, etc.).
     * @return el feed más reciente disponible para el tipo indicado.
     */
    Feed getNewestFeed(TipoSindicacion tipoSindicacion) throws MiServiceException;

    /**
     * Devuelve una lista de filtros de órganos de contratación a partir de una cláusula SQL.
     *
     * @param filtroSQL el SQL que representa los criterios de filtrado.
     * @return una lista de filtros aplicables a órganos de contratación.
     */
    List<FiltroOrganoContratacion> getListFiltroOcsFromFiltroSql(String filtroSQL) throws MiServiceException;
}

