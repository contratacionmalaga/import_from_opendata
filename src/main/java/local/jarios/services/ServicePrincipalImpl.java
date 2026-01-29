package local.jarios.services;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.database.SessionFactoryRegistry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.TipoConexion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.repositories.Repository;
import local.jarios.repositories.RepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class ServicePrincipalImpl implements ServicePrincipal, AutoCloseable {

  private final Repository repository;

  public ServicePrincipalImpl() throws MiServiceException {
    try {
      SessionFactory sessionFactory =
          SessionFactoryRegistry.getSessionFactory(TipoConexion.PRINCIPAL);
      this.repository = new RepositoryImpl(sessionFactory);
    } catch (HibernateException ex) {
      String msg = "Error al obtener la SessionFactory para la conexión MARIADB";
      log.error(msg, ex);
      throw new MiServiceException(msg, ex);
    }
  }

  private static void requireNonNull(Object obj, String msg) {
    Objects.requireNonNull(obj, msg);
  }

  @Override
  public void persistirLog(Log miLog) throws MiServiceException {
    requireNonNull(miLog, "miLog no puede ser null");
    executeVoid(() -> repository.persistirLog(miLog), "[persistirLog] - Error persistiendo Log");
  }

  @Override
  public void persistirConfiguracion(Configuracion configuracion) throws MiServiceException {
    requireNonNull(configuracion, "configuracion no puede ser null");
    executeVoid(() -> repository.persistirConfiguracion(configuracion),
                "[persistirConfiguracion] - Error persistiendo Configuracion");
  }

  @Override
  public void persistirListaNifFiltro(Log miLog, List<String> nifList) throws MiServiceException {
    requireNonNull(miLog, "miLog no puede ser null");
    requireNonNull(nifList, "nifList no puede ser null");

    executeVoid(() -> repository.persistirListaNifFiltro(miLog, nifList),
                "[persistirListaNifFiltro] - Error persistiendo lista NIF");
  }

  @Override
  public void persistirListaOcFiltro(Log miLog, List<OrganoContratacion> listOcsFiltro)
      throws MiServiceException {
    requireNonNull(miLog, "miLog no puede ser null");
    requireNonNull(listOcsFiltro, "listOcsFiltro no puede ser null");

    executeVoid(() -> repository.persistirListaOcFiltro(miLog, listOcsFiltro),
                "[persistirListaOcFiltro] - Error persistiendo lista OCs");
  }

  @Override
  public void persistirListaHistoricos(Log miLog, List<Historico> listHistorico)
      throws MiServiceException {
    requireNonNull(miLog, "miLog no puede ser null");
    requireNonNull(listHistorico, "listHistorico no puede ser null");

    executeVoid(() -> repository.persistirListaHistoricos(miLog, listHistorico),
                "[persistirListaHistoricos] - Error persistiendo lista Historicos");
  }

  @Override
  public void persistirEstadistica(Estadistica estadistica) throws MiServiceException {
    requireNonNull(estadistica, "estadistica no puede ser null");

    executeVoid(() -> repository.persistirEstadistica(estadistica),
                "[persistirEstadistica] - Error persistiendo Estadistica");
  }

  @Override
  public void persistirSetFeeds(Log miLog, Set<Feed> feedSet) throws MiServiceException {
    requireNonNull(miLog, "miLog no puede ser null");
    requireNonNull(feedSet, "feedSet no puede ser null");

    executeVoid(() -> repository.persistirSetFeeds(miLog, feedSet),
                "[persistirSetFeeds] - Error persistiendo Feeds");
  }

  // ===========================
  // Métodos reutilizables
  // ===========================

  @Override
  public Map<String, Entry> getMapEntries() throws MiServiceException {
    String sql = String.format(
        "SELECT e FROM Entry e " +
            "JOIN e.feed f " +
            "JOIN f.miLog l " +
            "WHERE l.tipoSindicacion = '%s'",
        VariablesGlobales.getTipoSindicacion()
    );

    return execute(() -> repository.getMapEntries(sql),
                   "[getMapEntries] - Error en la consulta: " + sql);
  }

  private <T> T execute(SupplierWithException<T> supplier, String errorMsg)
      throws MiServiceException {
    try {
      return supplier.get();
    } catch (RuntimeException ex) {
      log.error("{} - {}", errorMsg, ex.getMessage(), ex);
      throw new MiServiceException(errorMsg, ex);
    }
  }

  private void executeVoid(RunnableWithException runnable, String errorMsg)
      throws MiServiceException {
    try {
      runnable.run();
    } catch (RuntimeException ex) {
      log.error("{} - {}", errorMsg, ex.getMessage(), ex);
      throw new MiServiceException(errorMsg, ex);
    }
  }

  @Override
  public void close() {
    if (repository instanceof AutoCloseable ac) {
      try {
        ac.close();
      } catch (Exception ex) {
        log.warn("Error cerrando repository", ex);
      }
    }
  }

  @FunctionalInterface
  private interface SupplierWithException<T> {
    T get() throws MiRepositoryException;
  }

  @FunctionalInterface
  private interface RunnableWithException {
    void run() throws MiRepositoryException;
  }
}
