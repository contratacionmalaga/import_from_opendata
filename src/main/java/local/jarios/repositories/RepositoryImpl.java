package local.jarios.repositories;

import jakarta.persistence.TypedQuery;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.Nif;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.exceptions.MiRepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Implementación del repositorio que gestiona la persistencia y recuperación de datos mediante
 * Hibernate {@link SessionFactory}.
 */
@Slf4j
public class RepositoryImpl implements Repository, AutoCloseable {

  private final SessionFactory sessionFactory;

  public RepositoryImpl(SessionFactory sessionFactory) {
    this.sessionFactory = Objects.requireNonNull(sessionFactory,
                                                 "SessionFactory no puede ser null");
  }

  @Override
  public void close() {
    if (!sessionFactory.isClosed()) {
      sessionFactory.close();
      log.info("[RepositoryImpl] - SessionFactory cerrado correctamente.");
    }
  }

  public void persistirLog(Log miLog) {
    Objects.requireNonNull(miLog, "miLog no puede ser null");

    ejecutarEnTransaccion(session -> {
      session.persist(miLog);
      flushAndClear(session);
      log.debug("Persistido el registro Log: {}", miLog);
      return null;
    }, "persistirLog");
  }

  public void persistirConfiguracion(Configuracion configuracion) {
    Objects.requireNonNull(configuracion, "configuracion no puede ser null");

    ejecutarEnTransaccion(session -> {
      session.persist(configuracion);
      flushAndClear(session);
      log.debug("Persistido el registro Configuracion: {}", configuracion);
      return null;
    }, "persistirConfiguracion");
  }

  public void persistirListaNifFiltro(Log miLog, List<String> nifList) {
    Objects.requireNonNull(miLog, "miLog no puede ser null");
    Objects.requireNonNull(nifList, "nifList no puede ser null");

    ejecutarEnTransaccion(session -> {
      for (String nif : nifList) {
        Nif miNif = new Nif(nif);
        miNif.setMiLog(miLog);
        session.persist(miNif);
        log.debug("Persistido el Nif: {}", nif);
      }
      flushAndClear(session);
      return null;
    }, "persistirListaNifFiltro");
  }

  public void persistirListaOcFiltro(Log miLog, List<OrganoContratacion> listOrganosContratacionFiltro) {
    Objects.requireNonNull(miLog, "miLog no puede ser null");
    Objects.requireNonNull(listOrganosContratacionFiltro,
                           "listOrganosContratacionFiltro no puede ser null");

    ejecutarEnTransaccion(session -> {
      for (OrganoContratacion oc : listOrganosContratacionFiltro) {
        oc.setMiLog(miLog);
        session.persist(oc);
        log.debug("Persistido el OrganoContratacionFiltro: {}", oc);
      }
      flushAndClear(session);
      return null;
    }, "persistirListaOcFiltro");
  }

  public void persistirListaHistoricos(Log miLog, List<Historico> listHistoricos) {
    Objects.requireNonNull(miLog, "miLog no puede ser null");
    Objects.requireNonNull(listHistoricos, "listHistoricos no puede ser null");

    ejecutarEnTransaccion(session -> {

      int batchSize = getBatchSize();

      int contador = 0;

      for (Historico historico : listHistoricos) {
        historico.setMiLog(miLog);
        session.persist(historico);

        if (++contador % batchSize == 0) {
          session.flush();
          session.clear();
        }
      }

      // flush/clear final para los restos
      session.flush();
      session.clear();

      return null;
    }, "persistirListaHistoricos");
  }

  public void persistirEstadistica(Estadistica estadistica) {
    Objects.requireNonNull(estadistica, "estadistica no puede ser null");

    ejecutarEnTransaccion(session -> {
      session.merge(estadistica);
      flushAndClear(session);
      log.debug("Persistido el objeto Estadistica");
      return null;
    }, "persistirEstadistica");
  }

  public void persistirSetFeeds(Log miLog, Set<Feed> feedSet) {
    Objects.requireNonNull(miLog, "miLog no puede ser null");
    Objects.requireNonNull(feedSet, "feedSet no puede ser null");

    ejecutarEnTransaccion(session -> {

      int contadorEntry = 1;
      int contadorDeletedEntry = 1;

      long totalDeletedEntries = feedSet.stream()
          .filter(feed -> feed.getDeletedEntryList() != null)
          .mapToLong(feed -> feed.getDeletedEntryList().size())
          .sum();

      long totalEntries = feedSet.stream()
          .filter(feed -> feed.getEntryList() != null)
          .mapToLong(feed -> feed.getEntryList().size())
          .sum();

      for (Feed feed : feedSet) {

        List<Entry> entryList = feed.getEntryList();
        if (entryList == null) {
          entryList = List.of();
        }

        List<DeletedEntry> deletedEntryList = feed.getDeletedEntryList();
        if (deletedEntryList == null) {
          deletedEntryList = List.of();
        }

        feed.setMiLog(miLog);
        session.persist(feed);

        log.info("Persistido Feed - {}", feed.getLinkSelf());

        for (DeletedEntry deletedEntry : deletedEntryList) {
          deletedEntry.setFeed(feed);
          session.persist(deletedEntry);
          log.info("  Persistido DeletedEntry - {} - {}",
                   contadorDeletedEntry++ + "/" + totalDeletedEntries, deletedEntry.getRef());
        }

        flushAndClear(session);

        for (Entry entry : entryList) {
          entry.setFeed(feed);
          session.persist(entry);
          log.info("  Persistido Entry - {} - {}", contadorEntry++ + "/" + totalEntries,
                   entry.getEntryId());

          for (ContractFolderStatus cfs : entry.getContractFolderStatusList()) {
            cfs.setEntry(entry);
            session.persist(cfs);
            log.info("    Persistido ContractFolderStatus: {}", cfs.getContractFolderId());
          }
        }

        flushAndClear(session);
      }

      flushAndClear(session);
      return null;

    }, "persistirFeeds");
  }

  @Override
  public Map<String, Entry> getMapEntries(String sql) throws MiRepositoryException {
    Objects.requireNonNull(sql, "sql no puede ser null");

    return ejecutarEnTransaccion(session -> {
      TypedQuery<Entry> query = session.createQuery(sql, Entry.class);
      List<Entry> listEntries = query.getResultList();

      Map<String, Entry> mapEntries = new HashMap<>();
      if (listEntries != null) {
        for (Entry entry : listEntries) {
          mapEntries.put(entry.getEntryId(), entry);
        }
      }
      return mapEntries;
    }, "getMapEntries");
  }

  // ------------------------------------
  // MÉTODOS PRIVADOS
  // ------------------------------------

  private void flushAndClear(Session session) {
    session.flush();
    session.clear();
  }

  private int getBatchSize() {
    var batchSizeStr = (String) sessionFactory.getProperties().get("hibernate.jdbc.batch_size");
    if (batchSizeStr == null) {
      return 50; // valor por defecto seguro
    }
    return Integer.parseInt(batchSizeStr);
  }

  private <T> T ejecutarEnTransaccion(Function<Session, T> function, String metodo)
      throws MiRepositoryException {

    try (Session session = sessionFactory.openSession()) {
      Transaction transaction = session.beginTransaction();

      try {
        T result = function.apply(session);
        transaction.commit();
        return result;

      } catch (Exception ex) {
        log.error("[{}] - Error en transacción: {}", metodo, ex.getMessage(), ex);

        if (transaction != null && transaction.getStatus().canRollback()) {
          try {
            transaction.rollback();
            log.warn("[{}] - Transacción revertida debido a error", metodo);
          } catch (HibernateException rollbackEx) {
            log.error("[{}] - Error durante rollback: {}", metodo, rollbackEx.getMessage(),
                      rollbackEx);
          }
        }

        throw new MiRepositoryException("[" + metodo + "] - Error en transacción", ex);
      }
    }
  }
}
