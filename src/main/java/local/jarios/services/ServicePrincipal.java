package local.jarios.services;

import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiServiceException;

import java.util.Map;
import java.util.Set;

/**
 * Interfaz que define los métodos principales para la persistencia y recuperación
 * de información relacionada con la importación de datos abiertos.
 */
public interface ServicePrincipal {

    /**
     * Persiste un objeto de log en el sistema.
     *
     * @param miLog el objeto de log que se desea almacenar.
     * @param mapBaseDatos       el mapa que representa la base de datos con claves como IDs o nombres.
     * @param lugarImportacion   información sobre el origen o contexto de la importación.
     */
    void persistirMiLogLocal(Log miLog, Map<String, Entry> mapBaseDatos, LugarImportacion lugarImportacion) throws MiServiceException;

    /**
     * Persiste un objeto de log en el sistema.
     *
     * @param miLog el objeto de log que se desea almacenar.
     * @param setEntrysToDelete  conjunto de entrys a borrar de la base de datos
     */
    void persistirMiLogInternet(Log miLog, Set<String> setEntrysToDelete) throws MiServiceException;


    /**
     * Obtiene el feed más reciente correspondiente a un tipo específico de sindicación.
     *
     * @param tipoSindicacion el tipo de sindicación (RSS, Atom, etc.).
     * @return el feed más reciente disponible para el tipo indicado.
     */
    Feed getNewestFeed(TipoSindicacion tipoSindicacion) throws MiServiceException;

}

