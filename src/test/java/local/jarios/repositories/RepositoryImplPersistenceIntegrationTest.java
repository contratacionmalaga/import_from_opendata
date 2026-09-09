package local.jarios.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.database.EntityScanner;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.enums.DeletedEntryOpcion;
import local.jarios.enums.EntryOpcion;
import local.jarios.services.ImportPersistencePlan;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;

class RepositoryImplPersistenceIntegrationTest {

  @Test
  void replaces_existing_entry_preserving_first_created_at_inside_same_transaction()
      throws Exception {
    try (SessionFactory sessionFactory = newSessionFactory()) {
      RepositoryImpl repository = new RepositoryImpl(sessionFactory);
      seedExistingEntry(sessionFactory, "entry-1", "old-short", "old-title", "old-nif");
      LocalDateTime originalCreatedAt =
          singleDate(sessionFactory, "SELECT e.createdAt FROM Entry e WHERE e.entryId = 'entry-1'");

      Log importLog = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);
      Feed importFeed = feed("new-feed");
      Entry replacement = entry("entry-1", "old-short", "new-title", "new-nif");
      importFeed.getEntryList().add(replacement);
      HistoricoEntry actualizar = historicoEntry("entry-1", EntryOpcion.ACTUALIZAR);

      repository.persistirImportacion(
          new ImportPersistencePlan(
              importLog,
              null,
              List.of(),
              List.of(),
              Set.of(importFeed),
              List.of(actualizar),
              new Estadistica(importLog)));

      assertThat(count(sessionFactory, "SELECT COUNT(e) FROM Entry e WHERE e.entryId = 'entry-1'"))
          .isEqualTo(1L);
      assertThat(
              singleString(
                  sessionFactory, "SELECT e.title FROM Entry e WHERE e.entryId = 'entry-1'"))
          .isEqualTo("new-title");
      assertThat(
              singleString(
                  sessionFactory,
                  "SELECT c.nif FROM ContractFolderStatus c WHERE c.entry.entryId = 'entry-1'"))
          .isEqualTo("new-nif");
      assertThat(
              singleString(
                  sessionFactory,
                  "SELECT f.linkSelf FROM Entry e JOIN e.feed f WHERE e.entryId = 'entry-1'"))
          .isEqualTo("new-feed");
      assertThat(
              singleDate(
                  sessionFactory, "SELECT e.createdAt FROM Entry e WHERE e.entryId = 'entry-1'"))
          .isEqualTo(originalCreatedAt);
      assertThat(
              singleDate(
                  sessionFactory, "SELECT e.updatedAt FROM Entry e WHERE e.entryId = 'entry-1'"))
          .isAfterOrEqualTo(originalCreatedAt);
      assertThat(count(sessionFactory, "SELECT COUNT(h) FROM HistoricoEntry h")).isEqualTo(1L);
      assertThat(count(sessionFactory, "SELECT COUNT(e) FROM Estadistica e")).isEqualTo(1L);
    }
  }

  @Test
  void replaces_existing_entry_from_explicit_replacement_ids_without_persisting_historicos()
      throws Exception {
    try (SessionFactory sessionFactory = newSessionFactory()) {
      RepositoryImpl repository = new RepositoryImpl(sessionFactory);
      seedExistingEntry(sessionFactory, "entry-1", "old-short", "old-title", "old-nif");
      LocalDateTime originalCreatedAt =
          singleDate(sessionFactory, "SELECT e.createdAt FROM Entry e WHERE e.entryId = 'entry-1'");

      Log importLog = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);
      Feed importFeed = feed("new-feed");
      Entry replacement = entry("entry-1", "old-short", "new-title", "new-nif");
      importFeed.getEntryList().add(replacement);

      repository.persistirImportacion(
          new ImportPersistencePlan(
              importLog,
              null,
              List.of(),
              List.of(),
              Set.of(importFeed),
              Set.of("entry-1"),
              List.of(),
              new Estadistica(importLog)));

      assertThat(count(sessionFactory, "SELECT COUNT(e) FROM Entry e WHERE e.entryId = 'entry-1'"))
          .isEqualTo(1L);
      assertThat(
              singleString(
                  sessionFactory, "SELECT e.title FROM Entry e WHERE e.entryId = 'entry-1'"))
          .isEqualTo("new-title");
      assertThat(
              singleDate(
                  sessionFactory, "SELECT e.createdAt FROM Entry e WHERE e.entryId = 'entry-1'"))
          .isEqualTo(originalCreatedAt);
      assertThat(count(sessionFactory, "SELECT COUNT(h) FROM HistoricoEntry h")).isZero();
      assertThat(count(sessionFactory, "SELECT COUNT(e) FROM Estadistica e")).isEqualTo(1L);
    }
  }

  @Test
  void deduplicates_entries_inside_same_import_by_entry_id() throws Exception {
    try (SessionFactory sessionFactory = newSessionFactory()) {
      RepositoryImpl repository = new RepositoryImpl(sessionFactory);
      Log importLog = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);

      Feed firstFeed = feed("feed-1");
      firstFeed
          .getEntryList()
          .add(entry("duplicated-entry", "short-1", "title-1", "nif-1", "2026-01-02T10:00:00"));
      Feed secondFeed = feed("feed-2");
      secondFeed
          .getEntryList()
          .add(entry("duplicated-entry", "short-2", "title-2", "nif-2", "2026-01-03T10:00:00"));

      repository.persistirImportacion(
          new ImportPersistencePlan(
              importLog,
              null,
              List.of(),
              List.of(),
              Set.of(firstFeed, secondFeed),
              List.of(),
              new Estadistica(importLog)));

      assertThat(
              count(
                  sessionFactory,
                  "SELECT COUNT(e) FROM Entry e WHERE e.entryId = 'duplicated-entry'"))
          .isEqualTo(1L);
      assertThat(
              singleString(
                  sessionFactory,
                  "SELECT e.title FROM Entry e WHERE e.entryId = 'duplicated-entry'"))
          .isEqualTo("title-2");
      assertThat(
              singleString(
                  sessionFactory,
                  "SELECT c.nif FROM ContractFolderStatus c WHERE c.entry.entryId = 'duplicated-entry'"))
          .isEqualTo("nif-2");
    }
  }

  @Test
  void persists_entries_from_grouped_feed_plan_preserving_feed_relations_and_created_at()
      throws Exception {
    try (SessionFactory sessionFactory = newSessionFactory()) {
      RepositoryImpl repository = new RepositoryImpl(sessionFactory);
      seedExistingEntry(sessionFactory, "entry-1", "old-short", "old-title", "old-nif");
      LocalDateTime originalCreatedAt =
          singleDate(sessionFactory, "SELECT e.createdAt FROM Entry e WHERE e.entryId = 'entry-1'");

      Log importLog = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);
      Feed firstFeed = feed("grouped-feed-1");
      Feed secondFeed = feed("grouped-feed-2");
      List<Entry> firstEntries =
          new ArrayList<>(List.of(entry("entry-1", "old-short", "new-title", "new-nif")));
      List<Entry> secondEntries =
          new ArrayList<>(List.of(entry("entry-2", "short-2", "title-2", "nif-2")));

      repository.persistirImportacion(
          new ImportPersistencePlan(
              importLog,
              null,
              List.of(),
              List.of(),
              Set.of(firstFeed, secondFeed),
              Set.of("entry-1"),
              List.of(),
              Map.of(firstFeed, firstEntries, secondFeed, secondEntries),
              new Estadistica(importLog)));

      assertThat(count(sessionFactory, "SELECT COUNT(e) FROM Entry e")).isEqualTo(2L);
      assertThat(
              singleString(
                  sessionFactory,
                  "SELECT f.linkSelf FROM Entry e JOIN e.feed f WHERE e.entryId = 'entry-1'"))
          .isEqualTo("grouped-feed-1");
      assertThat(
              singleString(
                  sessionFactory,
                  "SELECT f.linkSelf FROM Entry e JOIN e.feed f WHERE e.entryId = 'entry-2'"))
          .isEqualTo("grouped-feed-2");
      assertThat(
              singleString(
                  sessionFactory,
                  "SELECT c.nif FROM ContractFolderStatus c WHERE c.entry.entryId = 'entry-1'"))
          .isEqualTo("new-nif");
      assertThat(
              singleDate(
                  sessionFactory, "SELECT e.createdAt FROM Entry e WHERE e.entryId = 'entry-1'"))
          .isEqualTo(originalCreatedAt);
      assertThat(count(sessionFactory, "SELECT COUNT(e) FROM Estadistica e")).isEqualTo(1L);
      assertThat(firstEntries).isEmpty();
      assertThat(secondEntries).isEmpty();
    }
  }

  @Test
  void inserts_deleted_entry_once_and_writes_insert_history() throws Exception {
    try (SessionFactory sessionFactory = newSessionFactory()) {
      RepositoryImpl repository = new RepositoryImpl(sessionFactory);
      Log importLog = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);
      Feed importFeed = feed("feed-with-deleted-entry");
      importFeed
          .getDeletedEntryList()
          .add(deletedEntry("https://example.test/licitacion-123", "licitacion-123"));

      repository.persistirImportacion(
          new ImportPersistencePlan(
              importLog,
              null,
              List.of(),
              List.of(),
              Set.of(importFeed),
              List.of(),
              new Estadistica(importLog)));

      assertThat(count(sessionFactory, "SELECT COUNT(d) FROM DeletedEntry d")).isEqualTo(1L);
      assertThat(singleString(sessionFactory, "SELECT d.refCorto FROM DeletedEntry d"))
          .isEqualTo("licitacion-123");
      assertThat(count(sessionFactory, "SELECT COUNT(h) FROM HistoricoDeletedEntry h"))
          .isEqualTo(1L);
      assertThat(singleDeletedEntryOpcion(sessionFactory)).isEqualTo(DeletedEntryOpcion.INSERTAR);
    }
  }

  @Test
  void updates_existing_deleted_entry_when_incoming_tombstone_is_newer() throws Exception {
    try (SessionFactory sessionFactory = newSessionFactory()) {
      RepositoryImpl repository = new RepositoryImpl(sessionFactory);
      seedExistingDeletedEntry(sessionFactory, "https://example.test/licitacion-123");
      LocalDateTime originalCreatedAt =
          singleDate(
              sessionFactory, "SELECT d.createdAt FROM DeletedEntry d WHERE d.ref LIKE '%123'");

      Log importLog = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);
      Feed importFeed = feed("new-deleted-feed");
      DeletedEntry newer = deletedEntry("https://example.test/licitacion-123", "licitacion-123");
      newer.setUpdated(LocalDateTime.parse("2026-01-04T10:00:00"));
      newer.setComment("new-comment");
      importFeed.getDeletedEntryList().add(newer);

      repository.persistirImportacion(
          new ImportPersistencePlan(
              importLog,
              null,
              List.of(),
              List.of(),
              Set.of(importFeed),
              List.of(),
              new Estadistica(importLog)));

      assertThat(
              count(sessionFactory, "SELECT COUNT(d) FROM DeletedEntry d WHERE d.ref LIKE '%123'"))
          .isEqualTo(1L);
      assertThat(
              singleString(
                  sessionFactory, "SELECT d.comment FROM DeletedEntry d WHERE d.ref LIKE '%123'"))
          .isEqualTo("new-comment");
      assertThat(
              singleString(
                  sessionFactory,
                  "SELECT f.linkSelf FROM DeletedEntry d JOIN d.feed f WHERE d.ref LIKE '%123'"))
          .isEqualTo("new-deleted-feed");
      assertThat(
              singleDate(
                  sessionFactory, "SELECT d.createdAt FROM DeletedEntry d WHERE d.ref LIKE '%123'"))
          .isEqualTo(originalCreatedAt);
      assertThat(singleDeletedEntryOpcion(sessionFactory)).isEqualTo(DeletedEntryOpcion.ACTUALIZAR);
    }
  }

  @Test
  void ignores_existing_deleted_entry_when_incoming_tombstone_is_not_newer() throws Exception {
    try (SessionFactory sessionFactory = newSessionFactory()) {
      RepositoryImpl repository = new RepositoryImpl(sessionFactory);
      seedExistingDeletedEntry(sessionFactory, "https://example.test/licitacion-123");

      Log importLog = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);
      Feed importFeed = feed("ignored-deleted-feed");
      DeletedEntry older = deletedEntry("https://example.test/licitacion-123", "licitacion-123");
      older.setUpdated(LocalDateTime.parse("2026-01-01T10:00:00"));
      older.setComment("ignored-comment");
      importFeed.getDeletedEntryList().add(older);

      repository.persistirImportacion(
          new ImportPersistencePlan(
              importLog,
              null,
              List.of(),
              List.of(),
              Set.of(importFeed),
              List.of(),
              new Estadistica(importLog)));

      assertThat(
              count(sessionFactory, "SELECT COUNT(d) FROM DeletedEntry d WHERE d.ref LIKE '%123'"))
          .isEqualTo(1L);
      assertThat(
              singleString(
                  sessionFactory, "SELECT d.comment FROM DeletedEntry d WHERE d.ref LIKE '%123'"))
          .isEqualTo("old-comment");
      assertThat(singleDeletedEntryOpcion(sessionFactory)).isEqualTo(DeletedEntryOpcion.IGNORAR);
    }
  }

  @Test
  void deduplicates_deleted_entries_inside_same_import_by_ref() throws Exception {
    try (SessionFactory sessionFactory = newSessionFactory()) {
      RepositoryImpl repository = new RepositoryImpl(sessionFactory);
      Log importLog = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);
      Feed firstFeed = feed("deleted-feed-1");
      DeletedEntry older = deletedEntry("https://example.test/licitacion-123", "licitacion-123");
      older.setUpdated(LocalDateTime.parse("2026-01-01T10:00:00"));
      older.setComment("older");
      firstFeed.getDeletedEntryList().add(older);
      Feed secondFeed = feed("deleted-feed-2");
      DeletedEntry newer = deletedEntry("https://example.test/licitacion-123", "licitacion-123");
      newer.setUpdated(LocalDateTime.parse("2026-01-05T10:00:00"));
      newer.setComment("newer");
      secondFeed.getDeletedEntryList().add(newer);

      repository.persistirImportacion(
          new ImportPersistencePlan(
              importLog,
              null,
              List.of(),
              List.of(),
              Set.of(firstFeed, secondFeed),
              List.of(),
              new Estadistica(importLog)));

      assertThat(
              count(sessionFactory, "SELECT COUNT(d) FROM DeletedEntry d WHERE d.ref LIKE '%123'"))
          .isEqualTo(1L);
      assertThat(
              singleString(
                  sessionFactory, "SELECT d.comment FROM DeletedEntry d WHERE d.ref LIKE '%123'"))
          .isEqualTo("newer");
    }
  }

  private static SessionFactory newSessionFactory() {
    Properties properties = new Properties();
    properties.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
    properties.setProperty(
        "hibernate.connection.url",
        "jdbc:h2:mem:"
            + UUID.randomUUID()
            + ";MODE=MariaDB;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
    properties.setProperty("hibernate.connection.username", "sa");
    properties.setProperty("hibernate.connection.password", "");
    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    properties.setProperty("hibernate.show_sql", "false");
    properties.setProperty("hibernate.format_sql", "false");
    properties.setProperty("hibernate.jdbc.batch_size", "20");

    Configuration configuration = new Configuration();
    configuration.setProperties(properties);
    new EntityScanner().scanAndAddEntities(configuration, "local.jarios.entity");
    return configuration.buildSessionFactory();
  }

  private static void seedExistingEntry(
      SessionFactory sessionFactory,
      String entryId,
      String entryIdCorto,
      String title,
      String nif) {
    sessionFactory.inTransaction(
        session -> {
          Log log = new Log(LugarImportacion.LOCAL, TipoSindicacion.MAYORES);
          session.persist(log);

          Feed feed = feed("old-feed");
          feed.setMiLog(log);
          session.persist(feed);

          Entry entry = entry(entryId, entryIdCorto, title, nif);
          entry.setFeed(feed);
          session.persist(entry);

          for (ContractFolderStatus cfs : entry.getContractFolderStatusList()) {
            cfs.setEntry(entry);
            session.persist(cfs);
          }
        });
  }

  private static void seedExistingDeletedEntry(SessionFactory sessionFactory, String ref) {
    sessionFactory.inTransaction(
        session -> {
          Log log = new Log(LugarImportacion.LOCAL, TipoSindicacion.MAYORES);
          session.persist(log);

          Feed feed = feed("old-deleted-feed");
          feed.setMiLog(log);
          session.persist(feed);

          DeletedEntry deletedEntry = deletedEntry(ref, "licitacion-123");
          deletedEntry.setComment("old-comment");
          deletedEntry.setFeed(feed);
          session.persist(deletedEntry);
        });
  }

  private static Feed feed(String linkSelf) {
    Feed feed = new Feed();
    feed.setLinkSelf(linkSelf);
    feed.setUpdated(LocalDateTime.parse("2026-01-01T10:00:00"));
    return feed;
  }

  private static Entry entry(String entryId, String entryIdCorto, String title, String nif) {
    return entry(entryId, entryIdCorto, title, nif, "2026-01-02T10:00:00");
  }

  private static Entry entry(
      String entryId, String entryIdCorto, String title, String nif, String updated) {
    Entry entry = new Entry();
    entry.setEntryId(entryId);
    entry.setEntryIdCorto(entryIdCorto);
    entry.setLink("https://example.test/" + entryIdCorto);
    entry.setTitle(title);
    entry.setSummary("summary " + title);
    entry.setUpdated(LocalDateTime.parse(updated));
    entry.getContractFolderStatusList().add(contractFolderStatus(entryIdCorto, nif));
    return entry;
  }

  private static DeletedEntry deletedEntry(String ref, String refCorto) {
    DeletedEntry deletedEntry = new DeletedEntry();
    deletedEntry.setRef(ref);
    deletedEntry.setRefCorto(refCorto);
    deletedEntry.setUpdated(LocalDateTime.parse("2026-01-03T10:00:00"));
    return deletedEntry;
  }

  private static ContractFolderStatus contractFolderStatus(String suffix, String nif) {
    ContractFolderStatus status = new ContractFolderStatus();
    status.setContractFolderId("folder-" + suffix);
    status.setContractFolderStatusCode("PUB");
    status.setIdPlataforma("platform-" + suffix);
    status.setNif(nif);
    return status;
  }

  private static HistoricoEntry historicoEntry(String entryId, EntryOpcion opcion) {
    HistoricoEntry historicoEntry = new HistoricoEntry();
    historicoEntry.setEntryId(entryId);
    historicoEntry.setEntryOpcion(opcion);
    historicoEntry.setEntryMotivo("test");
    return historicoEntry;
  }

  private static long count(SessionFactory sessionFactory, String query) {
    return sessionFactory.fromTransaction(
        session -> session.createQuery(query, Long.class).getSingleResult());
  }

  private static String singleString(SessionFactory sessionFactory, String query) {
    return sessionFactory.fromTransaction(
        session -> session.createQuery(query, String.class).getSingleResult());
  }

  private static LocalDateTime singleDate(SessionFactory sessionFactory, String query) {
    return sessionFactory.fromTransaction(
        session -> session.createQuery(query, LocalDateTime.class).getSingleResult());
  }

  private static DeletedEntryOpcion singleDeletedEntryOpcion(SessionFactory sessionFactory) {
    return sessionFactory.fromTransaction(
        session ->
            session
                .createQuery(
                    "SELECT h.opcion FROM HistoricoDeletedEntry h", DeletedEntryOpcion.class)
                .getSingleResult());
  }
}
