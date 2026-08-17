package local.jarios.repositories;

import jakarta.persistence.TypedQuery;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import local.jarios.core.enums.TipoSindicacion;
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
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus;
import local.jarios.enums.EntryOpcion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.services.ImportPersistencePlan;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Implementación del repositorio que gestiona la persistencia y recuperación de datos mediante
 * Hibernate {@link SessionFactory}.
 */
@Slf4j
public class RepositoryImpl implements Repository, AutoCloseable {

  private final SessionFactory sessionFactory;

  public RepositoryImpl(SessionFactory sessionFactory) {
    this.sessionFactory =
        Objects.requireNonNull(sessionFactory, "SessionFactory no puede ser null");
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

    ejecutarEnTransaccion(
        session -> {
          session.persist(miLog);
          flushAndClear(session);
          log.debug("Persistido el registro Log: {}", miLog);
          return null;
        },
        "persistirLog");
  }

  public void persistirConfiguracion(Configuracion configuracion) {
    Objects.requireNonNull(configuracion, "configuracion no puede ser null");

    ejecutarEnTransaccion(
        session -> {
          session.persist(configuracion);
          flushAndClear(session);
          log.debug("Persistido el registro Configuracion: {}", configuracion);
          return null;
        },
        "persistirConfiguracion");
  }

  public void persistirListaNifFiltro(Log miLog, List<String> nifList) {
    Objects.requireNonNull(miLog, "miLog no puede ser null");
    Objects.requireNonNull(nifList, "nifList no puede ser null");

    ejecutarEnTransaccion(
        session -> {
          for (String nif : nifList) {
            Nif miNif = new Nif(nif);
            miNif.setMiLog(miLog);
            session.persist(miNif);
            log.debug("Persistido el Nif: {}", nif);
          }
          flushAndClear(session);
          return null;
        },
        "persistirListaNifFiltro");
  }

  public void persistirListaOcFiltro(
      Log miLog, List<OrganoContratacion> listOrganosContratacionFiltro) {
    Objects.requireNonNull(miLog, "miLog no puede ser null");
    Objects.requireNonNull(
        listOrganosContratacionFiltro, "listOrganosContratacionFiltro no puede ser null");

    ejecutarEnTransaccion(
        session -> {
          for (OrganoContratacion oc : listOrganosContratacionFiltro) {
            oc.setMiLog(miLog);
            session.persist(oc);
            log.debug("Persistido el OrganoContratacionFiltro: {}", oc);
          }
          flushAndClear(session);
          return null;
        },
        "persistirListaOcFiltro");
  }

  public void persistirListaHistoricos(Log miLog, List<Historico> listHistoricos) {
    Objects.requireNonNull(miLog, "miLog no puede ser null");
    Objects.requireNonNull(listHistoricos, "listHistoricos no puede ser null");

    ejecutarEnTransaccion(
        session -> {
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
        },
        "persistirListaHistoricos");
  }

  public void persistirEstadistica(Estadistica estadistica) {
    Objects.requireNonNull(estadistica, "estadistica no puede ser null");

    ejecutarEnTransaccion(
        session -> {
          session.merge(estadistica);
          flushAndClear(session);
          log.debug("Persistido el objeto Estadistica");
          return null;
        },
        "persistirEstadistica");
  }

  public void persistirSetFeeds(Log miLog, Set<Feed> feedSet) {
    Objects.requireNonNull(miLog, "miLog no puede ser null");
    Objects.requireNonNull(feedSet, "feedSet no puede ser null");

    ejecutarEnTransaccion(
        session -> {
          int contadorEntry = 1;
          int contadorDeletedEntry = 1;

          long totalDeletedEntries =
              feedSet.stream()
                  .filter(feed -> feed.getDeletedEntryList() != null)
                  .mapToLong(feed -> feed.getDeletedEntryList().size())
                  .sum();

          long totalEntries =
              feedSet.stream()
                  .filter(feed -> feed.getEntryList() != null)
                  .mapToLong(feed -> feed.getEntryList().size())
                  .sum();

          log.info(
              "Persistiendo feeds en transaccion: feeds={}, entries={}, deletedEntries={}",
              feedSet.size(),
              totalEntries,
              totalDeletedEntries);
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

            log.debug("Persistido Feed - {}", feed.getLinkSelf());

            for (DeletedEntry deletedEntry : deletedEntryList) {
              deletedEntry.setFeed(feed);
              session.persist(deletedEntry);
              log.debug(
                  "  Persistido DeletedEntry - {} - {}",
                  contadorDeletedEntry++ + "/" + totalDeletedEntries,
                  deletedEntry.getRef());
            }

            flushAndClear(session);

            for (Entry entry : entryList) {
              entry.setFeed(feed);
              session.persist(entry);
              log.debug(
                  "  Persistido Entry - {} - {}",
                  contadorEntry++ + "/" + totalEntries,
                  entry.getEntryId());

              for (ContractFolderStatus cfs : entry.getContractFolderStatusList()) {
                cfs.setEntry(entry);
                session.persist(cfs);
                log.debug("    Persistido ContractFolderStatus: {}", cfs.getContractFolderId());
              }

              for (PreliminaryMarketConsultationStatus pmcs :
                  entry.getPreliminaryMarketConsultationStatusList()) {
                pmcs.setEntry(entry);
                session.persist(pmcs);
                log.debug(
                    "    Persistido PreliminaryMarketConsultationStatus: {}",
                    pmcs.getConsultationName());
              }
            }

            flushAndClear(session);
          }

          flushAndClear(session);
          return null;
        },
        "persistirFeeds");
  }

  @Override
  public void persistirImportacion(ImportPersistencePlan plan) throws MiRepositoryException {
    Objects.requireNonNull(plan, "plan no puede ser null");
    Objects.requireNonNull(plan.miLog(), "plan.miLog no puede ser null");
    Objects.requireNonNull(plan.feedSet(), "plan.feedSet no puede ser null");
    Objects.requireNonNull(plan.estadistica(), "plan.estadistica no puede ser null");

    ejecutarEnTransaccion(
        session -> {
          Log miLog = plan.miLog();
          session.persist(miLog);

          if (plan.configuracion() != null) {
            plan.configuracion().setMiLog(miLog);
            session.persist(plan.configuracion());
          }

          for (String nif : safeList(plan.nifList())) {
            Nif miNif = new Nif(nif);
            miNif.setMiLog(miLog);
            session.persist(miNif);
          }

          for (OrganoContratacion oc : safeList(plan.organoContratacionList())) {
            oc.setMiLog(miLog);
            session.persist(oc);
          }

          Set<String> updatedEntryIds = safeSet(plan.replacementEntryIds());
          log.info(
              "Persistencia importacion: feeds={}, historicos={}, reemplazos={}",
              plan.feedSet().size(),
              safeList(plan.historicoList()).size(),
              updatedEntryIds.size());
          deleteExistingEntriesForUpdates(session, updatedEntryIds);
          persistFeedsInCurrentTransaction(session, miLog, plan.feedSet());

          int batchSize = getBatchSize();
          int contador = 0;
          for (Historico historico : safeList(plan.historicoList())) {
            historico.setMiLog(miLog);
            session.persist(historico);
            if (++contador % batchSize == 0) {
              session.flush();
              session.clear();
            }
          }

          plan.estadistica().setMiLog(miLog);
          session.persist(plan.estadistica());
          flushAndClear(session);
          return null;
        },
        "persistirImportacion");
  }

  @Override
  public Map<String, EntrySnapshot> getEntrySnapshots(TipoSindicacion tipoSindicacion)
      throws MiRepositoryException {
    Objects.requireNonNull(tipoSindicacion, "tipoSindicacion no puede ser null");

    return ejecutarEnTransaccion(
        session -> {
          TypedQuery<EntrySnapshot> query =
              session.createQuery(
                  """
          SELECT new local.jarios.repositories.EntrySnapshot(e.entryId, e.updated)
          FROM Entry e
          JOIN e.feed f
          JOIN f.miLog l
          WHERE l.tipoSindicacion = :tipoSindicacion
          """,
                  EntrySnapshot.class);
          query.setParameter("tipoSindicacion", tipoSindicacion);
          List<EntrySnapshot> snapshots = query.getResultList();

          Map<String, EntrySnapshot> mapSnapshots = new HashMap<>();
          if (snapshots != null) {
            for (EntrySnapshot snapshot : snapshots) {
              mapSnapshots.put(snapshot.entryId(), snapshot);
            }
          }
          return mapSnapshots;
        },
        "getEntrySnapshots");
  }

  // ------------------------------------
  // MÉTODOS PRIVADOS
  // ------------------------------------

  @Override
  public long countEntries(TipoSindicacion tipoSindicacion) throws MiRepositoryException {
    Objects.requireNonNull(tipoSindicacion, "tipoSindicacion no puede ser null");

    return ejecutarEnTransaccion(
        session ->
            session
                .createQuery(
                    """
          SELECT COUNT(e)
          FROM Entry e
          JOIN e.feed f
          JOIN f.miLog l
          WHERE l.tipoSindicacion = :tipoSindicacion
          """,
                    Long.class)
                .setParameter("tipoSindicacion", tipoSindicacion)
                .getSingleResult(),
        "countEntries");
  }

  private void persistFeedsInCurrentTransaction(Session session, Log miLog, Set<Feed> feedSet) {
    int contadorEntry = 1;
    int contadorDeletedEntry = 1;

    long totalDeletedEntries =
        feedSet.stream()
            .filter(feed -> feed.getDeletedEntryList() != null)
            .mapToLong(feed -> feed.getDeletedEntryList().size())
            .sum();

    long totalEntries =
        feedSet.stream()
            .filter(feed -> feed.getEntryList() != null)
            .mapToLong(feed -> feed.getEntryList().size())
            .sum();

    log.info(
        "Persistiendo feeds en transaccion: feeds={}, entries={}, deletedEntries={}",
        feedSet.size(),
        totalEntries,
        totalDeletedEntries);

    int batchSize = getBatchSize();
    int persistedSinceFlush = 0;

    for (Feed feed : feedSet) {
      List<Entry> entryList = feed.getEntryList() == null ? List.of() : feed.getEntryList();
      List<DeletedEntry> deletedEntryList =
          feed.getDeletedEntryList() == null ? List.of() : feed.getDeletedEntryList();

      feed.setMiLog(miLog);
      session.persist(feed);
      persistedSinceFlush++;
      log.debug("Persistido Feed - {}", feed.getLinkSelf());

      for (DeletedEntry deletedEntry : deletedEntryList) {
        deletedEntry.setFeed(feed);
        session.persist(deletedEntry);
        persistedSinceFlush++;
        log.debug(
            "  Persistido DeletedEntry - {} - {}",
            contadorDeletedEntry++ + "/" + totalDeletedEntries,
            deletedEntry.getRef());
      }

      for (Entry entry : entryList) {
        entry.setFeed(feed);
        session.persist(entry);
        persistedSinceFlush++;
        log.debug(
            "  Persistido Entry - {} - {}",
            contadorEntry++ + "/" + totalEntries,
            entry.getEntryId());

        for (ContractFolderStatus cfs : entry.getContractFolderStatusList()) {
          cfs.setEntry(entry);
          session.persist(cfs);
          persistedSinceFlush++;
          log.debug("    Persistido ContractFolderStatus: {}", cfs.getContractFolderId());
        }

        for (PreliminaryMarketConsultationStatus pmcs :
            entry.getPreliminaryMarketConsultationStatusList()) {
          pmcs.setEntry(entry);
          session.persist(pmcs);
          persistedSinceFlush++;
          log.debug(
              "    Persistido PreliminaryMarketConsultationStatus: {}", pmcs.getConsultationName());
        }

        persistedSinceFlush = flushAndClearIfBatchReached(session, persistedSinceFlush, batchSize);
      }

      persistedSinceFlush = flushAndClearIfBatchReached(session, persistedSinceFlush, batchSize);
    }

    session.flush();
    session.clear();
  }

  static Set<String> entryIdsForOption(List<Historico> historicos, EntryOpcion opcion) {
    Objects.requireNonNull(opcion, "opcion no puede ser null");

    Set<String> entryIds = new HashSet<>();
    if (historicos == null) {
      return entryIds;
    }

    for (Historico historico : historicos) {
      if (historico == null || historico.getEntryOpcion() != opcion) {
        continue;
      }

      String entryId = historico.getEntryId();
      if (entryId != null && !entryId.isBlank()) {
        entryIds.add(entryId);
      }
    }

    return entryIds;
  }

  private void deleteExistingEntriesForUpdates(Session session, Set<String> updatedEntryIds) {
    if (updatedEntryIds == null || updatedEntryIds.isEmpty()) {
      return;
    }

    List<String> entryIds = List.copyOf(updatedEntryIds);
    int deleted = 0;
    int chunkSize = 500;

    for (int from = 0; from < entryIds.size(); from += chunkSize) {
      int to = Math.min(from + chunkSize, entryIds.size());
      deleted +=
          session
              .createMutationQuery("DELETE FROM Entry e WHERE e.entryId IN :entryIds")
              .setParameter("entryIds", entryIds.subList(from, to))
              .executeUpdate();
    }

    session.flush();
    log.info(
        "Entries existentes eliminados antes de ACTUALIZAR: solicitados={}, eliminados={}",
        updatedEntryIds.size(),
        deleted);
  }

  private int flushAndClearIfBatchReached(Session session, int persistedSinceFlush, int batchSize) {
    if (persistedSinceFlush < batchSize) {
      return persistedSinceFlush;
    }

    session.flush();
    session.clear();
    return 0;
  }

  private static <T> List<T> safeList(List<T> list) {
    return list == null ? List.of() : list;
  }

  private static <T> Set<T> safeSet(Set<T> set) {
    return set == null ? Set.of() : set;
  }

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
            log.error(
                "[{}] - Error durante rollback: {}", metodo, rollbackEx.getMessage(), rollbackEx);
          }
        }

        throw new MiRepositoryException("[" + metodo + "] - Error en transacción", ex);
      }
    }
  }
}
