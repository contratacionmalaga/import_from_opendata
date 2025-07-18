package local.jarios.services;

import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiServiceException;
import java.util.Map;

/**
 * Interfaz que define los métodos principales para la persistencia y recuperación
 * de información relacionada con la importación de datos abiertos.
 */
public interface ServicePrincipal {

    /**
     * Persiste un objeto de log en el sistema.
     *
     * @param miLog el objeto de log que se desea almacenar.
     */
    void persistirMiLogLocal(Log miLog) throws MiServiceException;

    /**
     * Obtiene el entry más reciente correspondiente a un tipo específico de sindicación.
     *
     * @param tipoSindicacion el tipo de sindicación (RSS, Atom, etc.).
     * @return el feed más reciente disponible para el tipo indicado.
     */
    Entry getNewestEntry(TipoSindicacion tipoSindicacion) throws MiServiceException;

    /**
     * Obtiene el feed más reciente correspondiente a un tipo específico de sindicación.
     *
     * @param tipoSindicacion el tipo de sindicación (RSS, Atom, etc.).
     * @return el feed más reciente disponible para el tipo indicado.
     */
    Map<String, Entry> getMapEntries(TipoSindicacion tipoSindicacion) throws MiServiceException;
}

