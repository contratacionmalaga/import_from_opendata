package local.jarios.repositories;

import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.exceptions.MiServiceException;

import java.util.List;
import java.util.Map;

/**
 * Interfaz que define las operaciones básicas para acceder y manipular datos relacionados con
 * entidades específicas del dominio, utilizando Hibernate.
 *
 * <p>Incluye métodos para persistencia de logs, ejecución de funciones
 * dentro de transacciones Hibernate, y acceso a entidades como {@link Entry}, {@link Feed} y
 * filtros de órganos de contratación.</p>
 */
public interface Repository {

  /**
   * Persiste un objeto {@link Log} en la base de datos.
   *
   * @param miLog Instancia de {@link Log} a persistir.
   * @throws MiRepositoryException Si ocurre un error durante la persistencia.
   */
  void persistirEnBaseDatos(Log miLog) throws MiRepositoryException;

  /**
   * Devuelve una lista de filtros de órganos de contratación construidos a partir de un filtro SQL
   * específico.
   *
   * @param filtroSQL Filtro SQL para filtrar los órganos de contratación.
   * @return Lista de {@link OrganoContratacion} que cumplen el filtro.
   * @throws MiRepositoryException Si ocurre un error en Hibernate durante la consulta.
   */
  List<OrganoContratacion> getListFiltroOcsFromFiltroSql(String filtroSQL) throws MiRepositoryException;

  /**
   * Obtiene el feed más reciente correspondiente a un tipo específico de sindicación.
   *
   * @param sql consulta a realizar sobre la base de datos
   * @return el feed más reciente disponible para el tipo indicado.
   */
  Map<String, Entry> getMapEntries(String sql) throws MiServiceException;
}
