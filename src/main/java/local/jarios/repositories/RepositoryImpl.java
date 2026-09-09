package local.jarios.repositories;

import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import local.jarios.entity.auxiliares.HistoricoDeletedEntry;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.Nif;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus;
import local.jarios.enums.DeletedEntryOpcion;
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

  private static final int QUERY_CHUNK_SIZE = 500;

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

  public void persistirListaHistoricos(Log miLog, List<HistoricoEntry> listHistoricos) {
    Objects.requireNonNull(miLog, "miLog no puede ser null");
    Objects.requireNonNull(listHistoricos, "listHistoricos no puede ser null");

    ejecutarEnTransaccion(
        session -> {
          int batchSize = getBatchSize();
          int contador = 0;

          for (HistoricoEntry historicoEntry : listHistoricos) {
            historicoEntry.setMiLog(miLog);
            session.persist(historicoEntry);

            if (++contador % batchSize == 0) {
              session.flush();
              session.clear();
            }
          }

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
          persistFeedsInCurrentTransaction(session, miLog, feedSet, Map.of(), null);
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
          Map<String, LocalDateTime> createdAtByEntryId =
              getCreatedAtByEntryId(session, updatedEntryIds);
          deleteExistingEntriesForUpdates(session, updatedEntryIds);
          persistFeedsInCurrentTransaction(
              session, miLog, plan.feedSet(), createdAtByEntryId, plan.entriesByFeed());
          persistHistoricosEntry(session, miLog, safeList(plan.historicoList()));

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

  private void persistFeedsInCurrentTransaction(
      Session session,
      Log miLog,
      Set<Feed> feedSet,
      Map<String, LocalDateTime> createdAtByEntryId,
      Map<Feed, List<Entry>> entriesByFeed) {
    Map<String, DeletedEntry> deletedEntriesByRef = latestDeletedEntriesByRef(feedSet);
    boolean hasPreGroupedEntries = entriesByFeed != null && !entriesByFeed.isEmpty();
    Map<String, Entry> entriesById = hasPreGroupedEntries ? Map.of() : latestEntriesById(feedSet);
    int entryCount = hasPreGroupedEntries ? countEntries(entriesByFeed) : entriesById.size();

    log.info(
        "Persistiendo feeds en transaccion: feeds={}, entries={}, deletedEntries={}",
        feedSet.size(),
        entryCount,
        deletedEntriesByRef.size());

    for (Feed feed : feedSet) {
      feed.setMiLog(miLog);
      session.persist(feed);
      log.debug("Persistido Feed - {}", feed.getLinkSelf());
    }

    persistDeletedEntries(session, miLog, deletedEntriesByRef.values());
    if (hasPreGroupedEntries) {
      persistEntriesByFeed(session, entriesByFeed, createdAtByEntryId);
    } else {
      persistEntries(session, entriesById.values(), createdAtByEntryId);
    }
    session.flush();
    session.clear();
  }

  private void persistEntries(
      Session session, Collection<Entry> entries, Map<String, LocalDateTime> createdAtByEntryId) {
    int contadorEntry = 1;
    int batchSize = getBatchSize();
    int persistedSinceFlush = 0;

    for (Entry entry : entries) {
      LocalDateTime originalCreatedAt = createdAtByEntryId.get(entry.getEntryId());
      if (originalCreatedAt != null) {
        entry.setCreatedAt(originalCreatedAt);
      }

      session.persist(entry);
      persistedSinceFlush++;
      log.debug(
          "  Persistido Entry - {} - {}",
          contadorEntry++ + "/" + entries.size(),
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
  }

  private void persistEntriesByFeed(
      Session session,
      Map<Feed, List<Entry>> entriesByFeed,
      Map<String, LocalDateTime> createdAtByEntryId) {
    for (Map.Entry<Feed, List<Entry>> feedEntries : entriesByFeed.entrySet()) {
      Feed feed = feedEntries.getKey();
      List<Entry> entries = safeList(feedEntries.getValue());
      for (Entry entry : entries) {
        entry.setFeed(feed);
      }
      persistEntries(session, entries, createdAtByEntryId);
      feed.setEntryList(new ArrayList<>());
      if (feedEntries.getValue() != null) {
        feedEntries.getValue().clear();
      }
    }
  }

  private int countEntries(Map<Feed, List<Entry>> entriesByFeed) {
    int count = 0;
    for (List<Entry> entries : entriesByFeed.values()) {
      count += safeList(entries).size();
    }
    return count;
  }

  private void persistDeletedEntries(
      Session session, Log miLog, Collection<DeletedEntry> incomingDeletedEntries) {
    Map<String, DeletedEntry> existingByRef =
        getDeletedEntriesByRef(session, refsFromDeletedEntries(incomingDeletedEntries));
    int contadorDeletedEntry = 1;

    for (DeletedEntry incoming : incomingDeletedEntries) {
      DeletedEntry existing = existingByRef.get(incoming.getRef());
      if (existing == null) {
        session.persist(incoming);
        persistHistoricoDeletedEntry(
            session,
            miLog,
            new HistoricoDeletedEntry(
                incoming, incoming, DeletedEntryOpcion.INSERTAR, "No existe deleted_entry previo"));
        existingByRef.put(incoming.getRef(), incoming);
      } else if (isIncomingNewer(incoming, existing)) {
        HistoricoDeletedEntry historico =
            new HistoricoDeletedEntry(
                existing,
                incoming,
                DeletedEntryOpcion.ACTUALIZAR,
                "Tombstone entrante mas reciente");
        existing.setFeed(incoming.getFeed());
        existing.setRefCorto(incoming.getRefCorto());
        existing.setUpdated(incoming.getUpdated());
        existing.setComment(incoming.getComment());
        persistHistoricoDeletedEntry(session, miLog, historico);
      } else {
        persistHistoricoDeletedEntry(
            session,
            miLog,
            new HistoricoDeletedEntry(
                existing,
                incoming,
                DeletedEntryOpcion.IGNORAR,
                "Tombstone entrante igual o anterior al existente"));
      }

      log.debug(
          "  Procesado DeletedEntry - {} - {}",
          contadorDeletedEntry++ + "/" + incomingDeletedEntries.size(),
          incoming.getRef());
    }
  }

  private void persistHistoricoDeletedEntry(
      Session session, Log miLog, HistoricoDeletedEntry historicoDeletedEntry) {
    historicoDeletedEntry.setMiLog(miLog);
    session.persist(historicoDeletedEntry);
  }

  private void persistHistoricosEntry(
      Session session, Log miLog, List<HistoricoEntry> historicoEntries) {
    int batchSize = getBatchSize();
    int contador = 0;
    for (HistoricoEntry historicoEntry : historicoEntries) {
      historicoEntry.setMiLog(miLog);
      session.persist(historicoEntry);
      if (++contador % batchSize == 0) {
        session.flush();
        session.clear();
      }
    }
  }

  private Map<String, Entry> latestEntriesById(Set<Feed> feedSet) {
    Map<String, Entry> entriesById = new LinkedHashMap<>();
    for (Feed feed : feedSet) {
      for (Entry entry : safeList(feed.getEntryList())) {
        if (entry == null || entry.getEntryId() == null || entry.getEntryId().isBlank()) {
          continue;
        }
        entry.setFeed(feed);
        entriesById.merge(entry.getEntryId(), entry, RepositoryImpl::latestEntry);
      }
    }
    return entriesById;
  }

  private Map<String, DeletedEntry> latestDeletedEntriesByRef(Set<Feed> feedSet) {
    Map<String, DeletedEntry> deletedEntriesByRef = new LinkedHashMap<>();
    for (Feed feed : feedSet) {
      for (DeletedEntry deletedEntry : safeList(feed.getDeletedEntryList())) {
        if (deletedEntry == null
            || deletedEntry.getRef() == null
            || deletedEntry.getRef().isBlank()) {
          continue;
        }
        deletedEntry.setFeed(feed);
        deletedEntriesByRef.merge(
            deletedEntry.getRef(), deletedEntry, RepositoryImpl::latestDeletedEntry);
      }
    }
    return deletedEntriesByRef;
  }

  private static Entry latestEntry(Entry current, Entry candidate) {
    if (isAfter(candidate.getUpdated(), current.getUpdated())) {
      return candidate;
    }
    return current;
  }

  private static DeletedEntry latestDeletedEntry(DeletedEntry current, DeletedEntry candidate) {
    if (isAfter(candidate.getUpdated(), current.getUpdated())) {
      return candidate;
    }
    return current;
  }

  private static boolean isIncomingNewer(DeletedEntry incoming, DeletedEntry existing) {
    return isAfter(incoming.getUpdated(), existing.getUpdated());
  }

  private static boolean isAfter(LocalDateTime candidate, LocalDateTime current) {
    if (candidate == null) {
      return false;
    }
    return current == null || candidate.isAfter(current);
  }

  private Set<String> refsFromDeletedEntries(Collection<DeletedEntry> deletedEntries) {
    Set<String> refs = new HashSet<>();
    for (DeletedEntry deletedEntry : deletedEntries) {
      if (deletedEntry.getRef() != null && !deletedEntry.getRef().isBlank()) {
        refs.add(deletedEntry.getRef());
      }
    }
    return refs;
  }

  private Map<String, DeletedEntry> getDeletedEntriesByRef(Session session, Set<String> refs) {
    Map<String, DeletedEntry> result = new HashMap<>();
    if (refs == null || refs.isEmpty()) {
      return result;
    }

    List<String> refList = List.copyOf(refs);
    for (int from = 0; from < refList.size(); from += QUERY_CHUNK_SIZE) {
      int to = Math.min(from + QUERY_CHUNK_SIZE, refList.size());
      List<DeletedEntry> deletedEntries =
          session
              .createQuery("FROM DeletedEntry d WHERE d.ref IN :refs", DeletedEntry.class)
              .setParameter("refs", refList.subList(from, to))
              .getResultList();
      for (DeletedEntry deletedEntry : deletedEntries) {
        result.put(deletedEntry.getRef(), deletedEntry);
      }
    }
    return result;
  }

  static Set<String> entryIdsForOption(List<HistoricoEntry> historicos, EntryOpcion opcion) {
    Objects.requireNonNull(opcion, "opcion no puede ser null");

    Set<String> entryIds = new HashSet<>();
    if (historicos == null) {
      return entryIds;
    }

    for (HistoricoEntry historicoEntry : historicos) {
      if (historicoEntry == null || historicoEntry.getEntryOpcion() != opcion) {
        continue;
      }

      String entryId = historicoEntry.getEntryId();
      if (entryId != null && !entryId.isBlank()) {
        entryIds.add(entryId);
      }
    }

    return entryIds;
  }

  private Map<String, LocalDateTime> getCreatedAtByEntryId(
      Session session, Set<String> updatedEntryIds) {
    Map<String, LocalDateTime> result = new HashMap<>();
    if (updatedEntryIds == null || updatedEntryIds.isEmpty()) {
      return result;
    }

    List<String> entryIds = List.copyOf(updatedEntryIds);
    for (int from = 0; from < entryIds.size(); from += QUERY_CHUNK_SIZE) {
      int to = Math.min(from + QUERY_CHUNK_SIZE, entryIds.size());
      List<Object[]> rows =
          session
              .createQuery(
                  "SELECT e.entryId, e.createdAt FROM Entry e WHERE e.entryId IN :entryIds",
                  Object[].class)
              .setParameter("entryIds", entryIds.subList(from, to))
              .getResultList();
      for (Object[] row : rows) {
        result.put((String) row[0], (LocalDateTime) row[1]);
      }
    }
    return result;
  }

  private void deleteExistingEntriesForUpdates(Session session, Set<String> updatedEntryIds) {
    if (updatedEntryIds == null || updatedEntryIds.isEmpty()) {
      return;
    }

    List<String> entryIds = List.copyOf(updatedEntryIds);
    int deleted = 0;

    for (int from = 0; from < entryIds.size(); from += QUERY_CHUNK_SIZE) {
      int to = Math.min(from + QUERY_CHUNK_SIZE, entryIds.size());
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
      return 50;
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
