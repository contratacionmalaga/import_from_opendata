package local.jarios.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.database.EntityScanner;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.TenderingProcess;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Prueba manual contra opendata_prueba que limpia sus datos temporales al terminar. */
@EnabledIfSystemProperty(named = "opendata.mariadb.fk.integration", matches = "true")
class MariaDbForeignKeyDeleteIntegrationTest {

  private static final String TEST_DATABASE = "opendata_prueba";

  @Test
  void bulk_delete_of_entry_removes_contract_folder_status_through_the_real_foreign_key()
      throws Exception {
    String marker = "f3-fk-" + UUID.randomUUID();
    try (SessionFactory sessionFactory = newSessionFactory()) {
      try {
        sessionFactory.inTransaction(session -> persistGraph(session, marker));

        assertThat(
                count(
                    sessionFactory,
                    "SELECT COUNT(c) FROM ContractFolderStatus c WHERE c.entry.entryId = :entryId",
                    marker))
            .isEqualTo(1L);
        assertThat(
                count(
                    sessionFactory,
                    "SELECT COUNT(t) FROM TenderingProcess t WHERE t.contractFolderStatus.entry.entryId = :entryId",
                    marker))
            .isEqualTo(1L);

        sessionFactory.inTransaction(
            session ->
                session
                    .createMutationQuery("DELETE FROM Entry e WHERE e.entryId = :entryId")
                    .setParameter("entryId", marker)
                    .executeUpdate());

        assertThat(
                count(
                    sessionFactory,
                    "SELECT COUNT(e) FROM Entry e WHERE e.entryId = :entryId",
                    marker))
            .isZero();
        assertThat(
                count(
                    sessionFactory,
                    "SELECT COUNT(c) FROM ContractFolderStatus c WHERE c.entry.entryId = :entryId",
                    marker))
            .isZero();
        assertThat(
                count(
                    sessionFactory,
                    "SELECT COUNT(t) FROM TenderingProcess t WHERE t.contractFolderStatus.entry.entryId = :entryId",
                    marker))
            .isZero();
      } finally {
        cleanup(sessionFactory, marker);
      }
    }
  }

  private static SessionFactory newSessionFactory() throws IOException {
    Properties properties = loadProperties("hibernate.properties");
    properties.putAll(loadProperties("bd.properties"));
    properties.setProperty(
        "jakarta.persistence.jdbc.url",
        databaseUrl(properties.getProperty("jakarta.persistence.jdbc.url")));
    properties.setProperty(
        "jakarta.persistence.jdbc.user", System.getProperty("opendata.mariadb.fk.user"));
    properties.setProperty(
        "jakarta.persistence.jdbc.password", System.getProperty("opendata.mariadb.fk.password"));
    properties.setProperty("hibernate.hbm2ddl.auto", "validate");

    Configuration configuration = new Configuration();
    configuration.setProperties(properties);
    new EntityScanner().scanAndAddEntities(configuration, "local.jarios.entity");
    return configuration.buildSessionFactory();
  }

  private static void persistGraph(org.hibernate.Session session, String marker) {
    Log log = new Log(LugarImportacion.LOCAL, TipoSindicacion.MAYORES);
    session.persist(log);

    Feed feed = new Feed();
    feed.setMiLog(log);
    feed.setLinkSelf("https://test.invalid/" + marker);
    feed.setUpdated(LocalDateTime.now());
    session.persist(feed);

    Entry entry = new Entry();
    entry.setEntryId(marker);
    entry.setEntryIdCorto(marker.substring(0, Math.min(50, marker.length())));
    entry.setLink("https://test.invalid/entry/" + marker);
    entry.setTitle("F3 FK integration test");
    entry.setUpdated(LocalDateTime.now());
    entry.setFeed(feed);
    session.persist(entry);

    ContractFolderStatus status = new ContractFolderStatus();
    status.setEntry(entry);
    status.setContractFolderId("folder-" + marker);
    status.setContractFolderStatusCode("PUB");
    status.setIdPlataforma("platform-" + marker);
    status.setNif("F3TEST");
    TenderingProcess tenderingProcess = new TenderingProcess();
    tenderingProcess.setContractFolderStatus(status);
    tenderingProcess.setOriginalContractingSystemId("F3-SYSTEM");
    tenderingProcess.setOriginalContractingSystemDescription("Sistema de prueba F3");
    tenderingProcess.setOriginalContractingSystemLotId("F3-LOT");
    tenderingProcess.setOriginalContractingSystemLotDescription("Lote de prueba F3");
    status.setTenderingProcess(tenderingProcess);
    session.persist(status);
  }

  private static void cleanup(SessionFactory sessionFactory, String marker) {
    sessionFactory.inTransaction(
        session -> {
          session
              .createMutationQuery(
                  "DELETE FROM ContractFolderStatus c WHERE c.entry.entryId = :entryId")
              .setParameter("entryId", marker)
              .executeUpdate();
          session
              .createMutationQuery("DELETE FROM Entry e WHERE e.entryId = :entryId")
              .setParameter("entryId", marker)
              .executeUpdate();
          session
              .createMutationQuery("DELETE FROM Feed f WHERE f.linkSelf = :linkSelf")
              .setParameter("linkSelf", "https://test.invalid/" + marker)
              .executeUpdate();
        });
  }

  private static long count(SessionFactory sessionFactory, String hql, String entryId) {
    return sessionFactory.fromTransaction(
        session ->
            session
                .createQuery(hql, Long.class)
                .setParameter("entryId", entryId)
                .getSingleResult());
  }

  private static Properties loadProperties(String name) throws IOException {
    Properties properties = new Properties();
    try (InputStream input = Files.newInputStream(Path.of("properties", name))) {
      properties.load(input);
    }
    return properties;
  }

  private static String databaseUrl(String configuredUrl) {
    return configuredUrl.replaceFirst("(?<=//[^/]+/)[^?]+", TEST_DATABASE);
  }
}
