package local.jarios.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
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
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.enums.EntryOpcion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.services.ImportPersistencePlan;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;

class RepositoryImplPersistenceIntegrationTest {

  @Test
  void replaces_existing_entry_marked_as_actualizar_inside_same_transaction() throws Exception {
    try (SessionFactory sessionFactory = newSessionFactory()) {
      RepositoryImpl repository = new RepositoryImpl(sessionFactory);
      seedExistingEntry(sessionFactory, "entry-1", "old-short", "old-title", "old-nif");

      Log importLog = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);
      Feed importFeed = feed("new-feed");
      Entry replacement = entry("entry-1", "old-short", "new-title", "new-nif");
      importFeed.getEntryList().add(replacement);
      Historico actualizar = historico("entry-1", EntryOpcion.ACTUALIZAR);

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
      assertThat(count(sessionFactory, "SELECT COUNT(h) FROM Historico h")).isEqualTo(1L);
      assertThat(count(sessionFactory, "SELECT COUNT(e) FROM Estadistica e")).isEqualTo(1L);
    }
  }

  @Test
  void replaces_existing_entry_from_explicit_replacement_ids_without_persisting_historicos()
      throws Exception {
    try (SessionFactory sessionFactory = newSessionFactory()) {
      RepositoryImpl repository = new RepositoryImpl(sessionFactory);
      seedExistingEntry(sessionFactory, "entry-1", "old-short", "old-title", "old-nif");

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
      assertThat(count(sessionFactory, "SELECT COUNT(h) FROM Historico h")).isZero();
      assertThat(count(sessionFactory, "SELECT COUNT(e) FROM Estadistica e")).isEqualTo(1L);
    }
  }

  @Test
  void persists_deleted_entry_ref_corto_inside_import_transaction() throws Exception {
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
    }
  }

  @Test
  void rolls_back_complete_import_when_persisting_duplicate_inserts_fails() throws Exception {
    try (SessionFactory sessionFactory = newSessionFactory()) {
      RepositoryImpl repository = new RepositoryImpl(sessionFactory);
      Log importLog = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);

      Feed firstFeed = feed("feed-1");
      firstFeed.getEntryList().add(entry("duplicated-entry", "short-1", "title-1", "nif-1"));
      Feed secondFeed = feed("feed-2");
      secondFeed.getEntryList().add(entry("duplicated-entry", "short-2", "title-2", "nif-2"));

      ImportPersistencePlan plan =
          new ImportPersistencePlan(
              importLog,
              null,
              List.of(),
              List.of(),
              Set.of(firstFeed, secondFeed),
              List.of(),
              new Estadistica(importLog));

      assertThatThrownBy(() -> repository.persistirImportacion(plan))
          .isInstanceOf(MiRepositoryException.class);

      assertThat(count(sessionFactory, "SELECT COUNT(l) FROM Log l")).isZero();
      assertThat(count(sessionFactory, "SELECT COUNT(f) FROM Feed f")).isZero();
      assertThat(count(sessionFactory, "SELECT COUNT(e) FROM Entry e")).isZero();
      assertThat(count(sessionFactory, "SELECT COUNT(e) FROM Estadistica e")).isZero();
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

  private static Feed feed(String linkSelf) {
    Feed feed = new Feed();
    feed.setLinkSelf(linkSelf);
    feed.setUpdated(LocalDateTime.parse("2026-01-01T10:00:00"));
    return feed;
  }

  private static Entry entry(String entryId, String entryIdCorto, String title, String nif) {
    Entry entry = new Entry();
    entry.setEntryId(entryId);
    entry.setEntryIdCorto(entryIdCorto);
    entry.setLink("https://example.test/" + entryIdCorto);
    entry.setTitle(title);
    entry.setSummary("summary " + title);
    entry.setUpdated(LocalDateTime.parse("2026-01-02T10:00:00"));
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

  private static Historico historico(String entryId, EntryOpcion opcion) {
    Historico historico = new Historico();
    historico.setEntryId(entryId);
    historico.setEntryOpcion(opcion);
    historico.setEntryMotivo("test");
    return historico;
  }

  private static long count(SessionFactory sessionFactory, String query) {
    return sessionFactory.fromTransaction(
        session -> session.createQuery(query, Long.class).getSingleResult());
  }

  private static String singleString(SessionFactory sessionFactory, String query) {
    return sessionFactory.fromTransaction(
        session -> session.createQuery(query, String.class).getSingleResult());
  }
}
