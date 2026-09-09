package local.jarios.repositories;

import java.util.List;
import java.util.Map;
import java.util.Set;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.services.ImportPersistencePlan;

/**
 * Interfaz que define las operaciones básicas para acceder y manipular datos relacionados con
 * entidades específicas del dominio, utilizando Hibernate.
 *
 * <p>Incluye métodos para persistencia de logs, ejecución de funciones dentro de transacciones
 * Hibernate, y acceso a entidades como {@link Entry}, {@link Feed} y filtros de órganos de
 * contratación.
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
  void persistirListaOcFiltro(Log miLog, List<OrganoContratacion> organoContratacionList)
      throws MiRepositoryException;

  /**
   * Persiste un objeto
   *
   * @param listHistoricos Instancia de
   * @throws MiRepositoryException Si ocurre un error durante la persistencia.
   */
  void persistirListaHistoricos(Log miLog, List<HistoricoEntry> listHistoricos)
      throws MiRepositoryException;

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
   * Persiste una importacion completa en una unica transaccion.
   *
   * @param plan datos de importacion a persistir.
   * @throws MiRepositoryException Si ocurre un error durante la persistencia.
   */
  void persistirImportacion(ImportPersistencePlan plan) throws MiRepositoryException;

  /**
   * Obtiene el feed más reciente correspondiente a un tipo específico de sindicación.
   *
   * @param tipoSindicacion tipo de sindicacion a filtrar.
   * @return snapshots historicos indexados por identificador externo.
   */
  Map<String, EntrySnapshot> getEntrySnapshots(TipoSindicacion tipoSindicacion)
      throws MiRepositoryException;

  /**
   * Cuenta las entradas existentes para un tipo de sindicacion.
   *
   * @param tipoSindicacion tipo de sindicacion a filtrar.
   * @return numero de entradas existentes.
   */
  long countEntries(TipoSindicacion tipoSindicacion) throws MiRepositoryException;
}
