package local.jarios.services;

import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.exceptions.MiServiceException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interfaz que define los métodos principales para la persistencia y recuperación de información
 * relacionada con la importación de datos abiertos.
 */
public interface ServicePrincipal {

  /**
   * Persiste un objeto de log junton con la configuracion en el sistema.
   *
   * @param miLog el objeto de log que se desea almacenar.
   */
  void persistirLog(Log miLog) throws MiServiceException;

  /**
   * Persiste un objeto de log junton con la configuracion en el sistema.
   *
   * @param configuracion el objeto de log que se desea almacenar.
   */
  void persistirConfiguracion(Configuracion configuracion) throws MiServiceException;

  /**
   * Actualiza el objeto log junton con las estadísticas en el sistema.
   *
   * @param organoContratacionList el objeto de log que se desea almacenar.
   */
  void persistirListaOcFiltro(Log miLog, List<OrganoContratacion> organoContratacionList) throws MiServiceException;

  /**
   * Actualiza el objeto log junton con las estadísticas en el sistema.
   *
   * @param nifList el objeto de log que se desea almacenar.
   */
  void persistirListaNifFiltro(Log miLog, List<String> nifList) throws MiServiceException;

  /**
   * Actualiza el objeto log junton con las estadísticas en el sistema.
   *
   * @param listHistorico el objeto de log que se desea almacenar.
   */
  void persistirListaHistoricos(Log miLog, List<Historico> listHistorico) throws MiServiceException;

  /**
   * Actualiza el objeto log junton con las estadísticas en el sistema.
   *
   * @param estadistica el objeto de log que se desea almacenar.
   */
  void persistirEstadistica(Estadistica estadistica) throws MiServiceException;

  /**
   * Persiste un objeto de log en el sistema.
   *
   * @param miLog el objeto de log que se desea almacenar.
   */
  void persistirSetFeeds(Log miLog, Set<Feed> feedSet) throws MiServiceException;

  /**
   * Obtiene el feed más reciente correspondiente a un tipo específico de sindicación.
   *
   * @return el feed más reciente disponible para el tipo indicado.
   */
  Map<String, Entry> getMapEntries() throws MiServiceException;
}
