package local.jarios.repositories;

import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.enums.LugarImportacion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.models.FiltroOrganoContratacion;

import java.util.List;
import java.util.Map;

/**
 * Interfaz que define las operaciones básicas para acceder y manipular datos
 * relacionados con entidades específicas del dominio, utilizando Hibernate.
 *
 * <p>Incluye métodos para persistencia de logs, ejecución de funciones
 * dentro de transacciones Hibernate, y acceso a entidades como {@link Entry},
 * {@link Feed} y filtros de órganos de contratación.</p>
 */
public interface Repository {

    /**
     * Persiste un objeto {@link Log} en la base de datos.
     *
     * @param miLog Instancia de {@link Log} a persistir.
     * @throws MiRepositoryException Si ocurre un error durante la persistencia.
     */
    void persistirLog(Log miLog, Map<String, Entry> mapBaseDatos, LugarImportacion lugarImportacion) throws MiRepositoryException;

    /**
     * Obtiene el {@link Feed} más reciente para un tipo específico de sindicacion.
     *
     * @param sql  Tipo de sindicacion para filtrar el feed.
     * @return El feed más nuevo encontrado o {@code null} si no existe.
     * @throws MiRepositoryException Si ocurre un error en Hibernate durante la consulta.
     */
    Feed getNewestFeed (String sql) throws MiRepositoryException;

    /**
     * Devuelve una lista de filtros de órganos de contratación construidos a partir
     * de un filtro SQL específico.
     *
     * @param filtroSQL Filtro SQL para filtrar los órganos de contratación.
     * @return Lista de {@link FiltroOrganoContratacion} que cumplen el filtro.
     * @throws MiRepositoryException Si ocurre un error en Hibernate durante la consulta.
     */
    List<FiltroOrganoContratacion> getListFiltroOcsFromFiltroSql (String filtroSQL) throws MiRepositoryException;
}
