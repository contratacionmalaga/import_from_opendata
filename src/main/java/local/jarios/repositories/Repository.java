package local.jarios.repositories;

import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.exceptions.MiServiceException;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
  void persistirLog(Log miLog) throws MiRepositoryException;

  /**
   * Persiste un objeto {@link Configuracion} en la base de datos.
   *
   * @param configuracion Instancia de {@link Configuracion} a persistir.
   * @throws MiRepositoryException Si ocurre un error durante la persistencia.
   */
  void persistirConfiguracion(Configuracion configuracion) throws MiRepositoryException;

  /**
   * Persiste un
   *
   * @param nifList Lista
   * @throws MiRepositoryException Si ocurre un error durante la persistencia.
   */
  void persistirListaNifFiltro(Log miLog, List<String> nifList) throws MiRepositoryException;

  /**
   * Persiste un
   *
   * @param organoContratacionList Lista
   * @throws MiRepositoryException Si ocurre un error durante la persistencia.
   */
  void persistirListaOcFiltro(Log miLog, List<OrganoContratacion> organoContratacionList) throws MiRepositoryException;

  /**
   * Persiste un objeto
   *
   * @param listHistoricos Instancia de
   * @throws MiRepositoryException Si ocurre un error durante la persistencia.
   */
  void persistirListaHistoricos(Log miLog, List<Historico> listHistoricos) throws MiRepositoryException;

  /**
   * Persiste un objeto {@link Estadistica} en la base de datos.
   *
   * @param estadistica Instancia de {@link Estadistica} a persistir.
   * @throws MiRepositoryException Si ocurre un error durante la persistencia.
   */
  void persistirEstadistica(Estadistica estadistica) throws MiRepositoryException;

  /**
   * Persiste un objeto {@link Log} en la base de datos.
   *
   * @param miLog Instancia de {@link Log} a persistir.
   * @throws MiRepositoryException Si ocurre un error durante la persistencia.
   */
  void persistirSetFeeds(Log miLog, Set<Feed> feedSet) throws MiRepositoryException;

  /**
   * Obtiene el feed más reciente correspondiente a un tipo específico de sindicación.
   *
   * @param sql consulta a realizar sobre la base de datos
   * @return el feed más reciente disponible para el tipo indicado.
   */
  Map<String, Entry> getMapEntries(String sql) throws MiServiceException;
}
