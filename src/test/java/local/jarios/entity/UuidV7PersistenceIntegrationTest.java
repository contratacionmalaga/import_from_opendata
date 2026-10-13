package local.jarios.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.database.EntityScanner;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.DocumentReference;
import local.jarios.entity.codice.ProcurementProjectLot;
import local.jarios.repositories.RepositoryImpl;
import local.jarios.services.ImportPersistencePlan;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Pruebas de generación real y round-trip; las inserciones se revierten al terminar. */
class UuidV7PersistenceIntegrationTest {

  private static SessionFactory sessionFactory;
  private static final boolean MARIADB = Boolean.getBoolean("opendata.uuid.mariadb");

  @BeforeAll
  static void openDatabase() throws Exception {
    sessionFactory = newSessionFactory();
  }

  @AfterAll
  static void closeDatabase() {
    if (sessionFactory != null) {
      sessionFactory.close();
    }
  }

  @Test
  void all_33_new_identifiers_and_every_populated_foreign_key_are_native_unique_uuid_v7()
      throws Exception {
    Map<Class<?>, Object> entities = fixture();
    assertThat(entities).hasSize(33);
    try (Session session = sessionFactory.openSession()) {
      var transaction = session.beginTransaction();
      try {
        Set<Object> persisted = new HashSet<>();
        for (Object entity : entities.values()) {
          persistParentsFirst(session, entity, persisted);
        }
        session.flush();
        Set<UUID> identifiers = new HashSet<>();
        int foreignKeys = 0;
        for (Object entity : entities.values()) {
          Field id = entity.getClass().getDeclaredField("id");
          id.setAccessible(true);
          assertThat(id.getAnnotation(UuidGenerator.class).style())
              .isEqualTo(UuidGenerator.Style.VERSION_7);
          UUID identifier = (UUID) id.get(entity);
          assertThat(identifier.version()).as(entity.getClass().getSimpleName()).isEqualTo(7);
          assertThat(identifier.variant()).isEqualTo(2);
          assertThat(identifiers.add(identifier)).isTrue();
          String table = entity.getClass().getAnnotation(Table.class).name();
          assertStoredUuid(session, table, "id", identifier, identifier);
          session.doWork(
              connection -> {
                try (var primaryKey =
                    connection.getMetaData().getPrimaryKeys(connection.getCatalog(), null, table)) {
                  assertThat(primaryKey.next()).as(table + " tiene PK").isTrue();
                  assertThat(primaryKey.getString("COLUMN_NAME")).isEqualTo("id");
                  assertThat(primaryKey.next()).isFalse();
                }
              });
          for (Field field : entity.getClass().getDeclaredFields()) {
            JoinColumn join = field.getAnnotation(JoinColumn.class);
            if (join != null) {
              field.setAccessible(true);
              Object parent = field.get(entity);
              Field parentId = parent.getClass().getDeclaredField("id");
              parentId.setAccessible(true);
              UUID expected = (UUID) parentId.get(parent);
              assertStoredUuid(session, table, join.name(), identifier, expected);
              String parentTable = parent.getClass().getAnnotation(Table.class).name();
              session.doWork(
                  connection -> {
                    Map<String, String> references = new LinkedHashMap<>();
                    try (var constraints =
                        connection
                            .getMetaData()
                            .getImportedKeys(connection.getCatalog(), null, table)) {
                      while (constraints.next()) {
                        references.put(
                            constraints.getString("FKCOLUMN_NAME"),
                            constraints.getString("PKTABLE_NAME")
                                + "."
                                + constraints.getString("PKCOLUMN_NAME"));
                      }
                    }
                    assertThat(references).containsEntry(join.name(), parentTable + ".id");
                  });
              foreignKeys++;
            }
          }
        }
        System.out.println(
            "UUID v7 verificados: "
                + identifiers.size()
                + " IDs y "
                + foreignKeys
                + " FK; destino="
                + (MARIADB ? "opendata_malaga_testing:3306" : "H2 efímera"));
        // También se releen los campos externos sin interpretar sus valores como UUID internos.
        session.clear();
        for (Object entity : entities.values()) {
          Field id = entity.getClass().getDeclaredField("id");
          id.setAccessible(true);
          Object reloaded = Hibernate.unproxy(session.find(entity.getClass(), id.get(entity)));
          for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.getType() == String.class && field.isAnnotationPresent(Column.class)) {
              field.setAccessible(true);
              assertThat(field.get(reloaded))
                  .as(entity.getClass().getSimpleName() + "." + field.getName())
                  .isEqualTo(field.get(entity));
            }
          }
        }
      } finally {
        transaction.rollback();
      }
    }
  }

  @Test
  void repeated_and_updated_imports_keep_the_identifiers_of_the_entire_codice_graph()
      throws Exception {
    String marker = "uuid7-" + UUID.randomUUID();
    List<UUID> logs = new ArrayList<>();
    try {
      Map<Class<?>, Object> initial = treeFixture(marker);
      ContractFolderStatus initialStatus =
          (ContractFolderStatus) initial.get(ContractFolderStatus.class);
      DocumentReference removedDocument = new DocumentReference();
      removedDocument.setDocumentReferenceId("remove-me");
      removedDocument.setAdditionalDocumentReference(initialStatus);
      initialStatus.getAdditionalDocumentReferenceList().add(removedDocument);
      Log initialLog = (Log) initial.get(Log.class);
      sessionFactory.inTransaction(
          session -> {
            try {
              Set<Object> persisted = new HashSet<>();
              for (Object entity : initial.values()) {
                persistParentsFirst(session, entity, persisted);
              }
            } catch (Exception e) {
              throw new IllegalStateException(e);
            }
          });
      logs.add(initialLog.getId());
      Map<Class<?>, UUID> expected = identifiers(initial);
      UUID addedDocumentId = null;
      for (int run = 0; run < 2; run++) {
        Map<Class<?>, Object> incoming = treeFixture(marker);
        Entry replacement = (Entry) incoming.get(Entry.class);
        replacement.setTitle("Actualización " + run);
        ((DocumentReference) incoming.get(DocumentReference.class)).setFilename("actualizado.pdf");
        for (Object entity : incoming.values()) {
          try {
            Field description = entity.getClass().getDeclaredField("description");
            description.setAccessible(true);
            description.set(entity, "Descripción actualizada " + run);
          } catch (NoSuchFieldException ignored) {
            // No todas las entidades contienen una descripción.
          }
        }
        ContractFolderStatus incomingStatus =
            (ContractFolderStatus) incoming.get(ContractFolderStatus.class);
        DocumentReference addedDocument = new DocumentReference();
        addedDocument.setDocumentReferenceId("new-document");
        addedDocument.setAdditionalDocumentReference(incomingStatus);
        incomingStatus.getAdditionalDocumentReferenceList().add(addedDocument);
        java.util.Collections.reverse(incomingStatus.getAdditionalDocumentReferenceList());
        Feed feed = replacement.getFeed();
        Log log = new Log(LugarImportacion.LOCAL, TipoSindicacion.MAYORES);
        new RepositoryImpl(sessionFactory)
            .persistirImportacion(
                new ImportPersistencePlan(
                    log,
                    null,
                    List.of(),
                    List.of(),
                    Set.of(feed),
                    Set.of(marker),
                    List.of(),
                    new Estadistica(log)));
        logs.add(log.getId());
        sessionFactory.inTransaction(
            session -> {
              for (Map.Entry<Class<?>, UUID> identity : expected.entrySet()) {
                if (identity.getKey() == Entry.class
                    || identity.getKey().getPackageName().endsWith("codice")) {
                  Object stored = session.find(identity.getKey(), identity.getValue());
                  assertThat(stored)
                      .as(identity.getKey().getSimpleName() + " conserva su UUID")
                      .isNotNull();
                }
              }
              Entry stored = session.find(Entry.class, expected.get(Entry.class));
              assertThat(stored.getTitle()).isEqualTo(replacement.getTitle());
              assertThat(stored.getEntryId()).isEqualTo(marker);
              DocumentReference document =
                  session.find(DocumentReference.class, expected.get(DocumentReference.class));
              assertThat(document.getDocumentReferenceId()).isEqualTo("z7");
              assertThat(document.getFilename()).isEqualTo("actualizado.pdf");
              assertThat(
                      session
                          .find(
                              ProcurementProjectLot.class,
                              expected.get(ProcurementProjectLot.class))
                          .getLote())
                  .isEqualTo("z7");
              assertThat(
                      session
                          .find(
                              local.jarios.entity.codice.Uuid.class,
                              expected.get(local.jarios.entity.codice.Uuid.class))
                          .getUuid())
                  .isEqualTo("z7");
              assertThat(session.find(DocumentReference.class, removedDocument.getId())).isNull();
              assertThat(
                      session
                          .find(
                              ContractFolderStatus.class, expected.get(ContractFolderStatus.class))
                          .getAdditionalDocumentReferenceList())
                  .hasSize(2);
              for (Map.Entry<Class<?>, UUID> identity : expected.entrySet()) {
                if (identity.getKey().getPackageName().endsWith("codice")) {
                  String table = identity.getKey().getAnnotation(Table.class).name();
                  assertStoredUuid(session, table, "id", identity.getValue(), identity.getValue());
                }
              }
            });
        UUID currentAddedId =
            sessionFactory.fromTransaction(
                session ->
                    session
                        .createQuery(
                            "SELECT d.id FROM DocumentReference d WHERE d.additionalDocumentReference.entry.entryId = :entry AND d.documentReferenceId = 'new-document'",
                            UUID.class)
                        .setParameter("entry", marker)
                        .getSingleResult());
        assertThat(currentAddedId.version()).isEqualTo(7);
        if (addedDocumentId != null) {
          assertThat(currentAddedId).isEqualTo(addedDocumentId);
        }
        addedDocumentId = currentAddedId;
      }
    } finally {
      cleanupLogs(logs);
    }
  }

  @Test
  void updating_legacy_v4_rows_keeps_their_identity_and_generates_v7_only_for_new_children()
      throws Exception {
    String marker = "legacy-" + UUID.randomUUID();
    UUID entryId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    List<UUID> logs = new ArrayList<>();
    try {
      Log originalLog = new Log(LugarImportacion.LOCAL, TipoSindicacion.MAYORES);
      Feed originalFeed = new Feed();
      originalFeed.setLinkSelf(marker);
      originalFeed.setUpdated(LocalDateTime.now());
      originalFeed.setMiLog(originalLog);
      sessionFactory.inTransaction(
          session -> {
            session.persist(originalLog);
            session.persist(originalFeed);
            session.flush();
            session
                .createNativeMutationQuery(
                    "INSERT INTO entry (id, entry_id, entry_id_corto, link, updated, created_at, updated_at, feed_id) VALUES (:id, :external, :external, :external, :time, :time, :time, :feed)")
                .setParameter("id", entryId)
                .setParameter("external", marker)
                .setParameter("time", LocalDateTime.now())
                .setParameter("feed", originalFeed.getId())
                .executeUpdate();
            session
                .createNativeMutationQuery(
                    "INSERT INTO contract_folder_status (id, contract_folder_id, contract_folder_status_code, entry_id) VALUES (:id, 'z7', 'PUB', :entry)")
                .setParameter("id", statusId)
                .setParameter("entry", entryId)
                .executeUpdate();
          });
      logs.add(originalLog.getId());
      Map<Class<?>, Object> incoming = treeFixture(marker);
      Feed feed = ((Entry) incoming.get(Entry.class)).getFeed();
      Log log = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);
      new RepositoryImpl(sessionFactory)
          .persistirImportacion(
              new ImportPersistencePlan(
                  log,
                  null,
                  List.of(),
                  List.of(),
                  Set.of(feed),
                  Set.of(marker),
                  List.of(),
                  new Estadistica(log)));
      logs.add(log.getId());
      sessionFactory.inTransaction(
          session -> {
            Entry stored = session.find(Entry.class, entryId);
            ContractFolderStatus status = session.find(ContractFolderStatus.class, statusId);
            assertThat(stored).isNotNull();
            assertThat(status).isNotNull();
            assertThat(stored.getId().version()).isEqualTo(4);
            assertStoredUuid(session, "entry", "id", entryId, entryId);
            assertStoredUuid(session, "contract_folder_status", "entry_id", statusId, entryId);
            assertThat(status.getEntry().getId()).isEqualTo(entryId);
            assertThat(status.getId().version()).isEqualTo(4);
            assertThat(status.getTenderingProcess().getId().version()).isEqualTo(7);
            assertThat(status.getTenderingProcess().getContractFolderStatus().getId())
                .isEqualTo(statusId);
            assertThat(status.getUuidList().getFirst().getUuid()).isEqualTo("z7");
            assertThat(status.getUuidList().getFirst().getId().version()).isEqualTo(7);
          });
    } finally {
      cleanupLogs(logs);
    }
  }

  private static void cleanupLogs(List<UUID> ids) {
    if (!ids.isEmpty()) {
      sessionFactory.inTransaction(
          session ->
              session
                  .createMutationQuery("DELETE FROM Log l WHERE l.id IN :ids")
                  .setParameter("ids", ids)
                  .executeUpdate());
    }
  }

  private static Map<Class<?>, UUID> identifiers(Map<Class<?>, Object> entities) throws Exception {
    Map<Class<?>, UUID> result = new LinkedHashMap<>();
    for (Object entity : entities.values()) {
      Field id = entity.getClass().getDeclaredField("id");
      id.setAccessible(true);
      result.put(entity.getClass(), (UUID) id.get(entity));
    }
    return result;
  }

  private static Map<Class<?>, Object> treeFixture(String marker) throws Exception {
    Map<Class<?>, Object> entities = fixture();
    Entry entry = (Entry) entities.get(Entry.class);
    entry.setEntryId(marker);
    entry.setEntryIdCorto(marker);
    // Cada entidad escoge un único padre para formar un árbol, incluidos los casos CPM.
    for (Object entity : entities.values()) {
      boolean hasRequiredParent =
          java.util.Arrays.stream(entity.getClass().getDeclaredFields())
              .anyMatch(
                  field ->
                      field.isAnnotationPresent(JoinColumn.class)
                          && !field.getAnnotation(JoinColumn.class).nullable());
      boolean parentSelected = false;
      for (Field field : entity.getClass().getDeclaredFields()) {
        if (field.isAnnotationPresent(JoinColumn.class)) {
          field.setAccessible(true);
          if (hasRequiredParent
              ? field.getAnnotation(JoinColumn.class).nullable()
              : parentSelected) {
            field.set(entity, null);
          } else {
            parentSelected = true;
          }
        }
      }
    }
    for (Object parent : entities.values()) {
      for (Field field : parent.getClass().getDeclaredFields()) {
        OneToMany many = field.getAnnotation(OneToMany.class);
        OneToOne one = field.getAnnotation(OneToOne.class);
        String mappedBy = many != null ? many.mappedBy() : one != null ? one.mappedBy() : "";
        if (mappedBy.isBlank()) {
          continue;
        }
        field.setAccessible(true);
        for (Object child : entities.values()) {
          Field backReference;
          try {
            backReference = child.getClass().getDeclaredField(mappedBy);
          } catch (NoSuchFieldException ignored) {
            continue;
          }
          backReference.setAccessible(true);
          Class<?> childType =
              many != null
                  ? (Class<?>)
                      ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]
                  : field.getType();
          if (childType.isInstance(child) && backReference.get(child) == parent) {
            if (many != null) {
              @SuppressWarnings("unchecked")
              Collection<Object> children = (Collection<Object>) field.get(parent);
              if (children == null) {
                children = new ArrayList<>();
                field.set(parent, children);
              }
              children.add(child);
            } else if (field.getType().isInstance(child)) {
              field.set(parent, child);
            }
          }
        }
      }
    }
    return entities;
  }

  private static void assertStoredUuid(
      Session session, String table, String column, UUID rowId, UUID expected) {
    session.doWork(
        connection -> {
          try (var statement =
              connection.prepareStatement(
                  "SELECT `" + column + "` FROM `" + table + "` WHERE id = ?")) {
            statement.setObject(1, rowId);
            try (var result = statement.executeQuery()) {
              assertThat(result.next()).isTrue();
              assertThat(result.getMetaData().getColumnTypeName(1)).isEqualToIgnoringCase("UUID");
              UUID actual = result.getObject(1, UUID.class);
              assertThat(actual).as(table + "." + column).isEqualTo(expected);
              assertThat(actual.version()).isEqualTo(expected.version());
              assertThat(result.next()).isFalse();
            }
          }
        });
  }

  private static Map<Class<?>, Object> fixture() throws Exception {
    Map<Class<?>, Object> entities = new LinkedHashMap<>();
    for (Class<?> type : EntityScanner.findEntities("local.jarios.entity")) {
      Field id = type.getDeclaredField("id");
      if (id.isAnnotationPresent(GeneratedValue.class)) {
        entities.put(type, type.getDeclaredConstructor().newInstance());
      }
    }
    for (Object entity : entities.values()) {
      for (Field field : entity.getClass().getDeclaredFields()) {
        field.setAccessible(true);
        Column column = field.getAnnotation(Column.class);
        if (column != null && field.getType() == String.class) {
          // Algunos códigos tienen longitud 2; el valor incluye caracteres no hexadecimales.
          field.set(entity, "z7");
        } else if (column != null && field.getType() == LocalDateTime.class) {
          field.set(entity, LocalDateTime.of(2026, 10, 13, 22, 16));
        } else if (column != null && field.getType() == LocalDate.class) {
          field.set(entity, LocalDate.of(2026, 10, 13));
        } else if (column != null && field.getType().isEnum()) {
          field.set(entity, field.getType().getEnumConstants()[0]);
        } else if (field.isAnnotationPresent(JoinColumn.class)) {
          field.set(entity, entities.get(field.getType()));
        }
      }
    }
    return entities;
  }

  private static void persistParentsFirst(Session session, Object entity, Set<Object> persisted)
      throws Exception {
    if (!persisted.add(entity)) {
      return;
    }
    for (Field field : entity.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(JoinColumn.class)) {
        field.setAccessible(true);
        if (field.get(entity) != null) {
          persistParentsFirst(session, field.get(entity), persisted);
        }
      }
    }
    session.persist(entity);
  }

  private static SessionFactory newSessionFactory() throws Exception {
    Properties properties = new Properties();
    if (MARIADB) {
      Path connectionFile =
          Path.of(System.getProperty("opendata.uuid.properties", "properties/bd.properties"));
      try (InputStream input = Files.newInputStream(connectionFile)) {
        properties.load(input);
      }
      // Destino fijo de pruebas: no se utiliza la base operativa indicada en los properties.
      properties.setProperty(
          "jakarta.persistence.jdbc.url", "jdbc:mariadb://localhost:3306/opendata_malaga_testing");
      properties.setProperty("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
      properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
      try (Connection connection =
          DriverManager.getConnection(
              properties.getProperty("jakarta.persistence.jdbc.url"),
              properties.getProperty("jakarta.persistence.jdbc.user"),
              properties.getProperty("jakarta.persistence.jdbc.password"))) {
        List<String> tables = new ArrayList<>();
        try (var result =
            connection
                .getMetaData()
                .getTables(connection.getCatalog(), null, "%", new String[] {"TABLE"})) {
          while (result.next()) {
            tables.add(result.getString("TABLE_NAME"));
          }
        }
        // Solo se crea el esquema ORM cuando la base está vacía. Nunca se borran tablas.
        properties.setProperty(
            "hibernate.hbm2ddl.auto", tables.isEmpty() ? "create-only" : "validate");
      }
    } else {
      properties.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
      properties.setProperty(
          "hibernate.connection.url",
          "jdbc:h2:mem:uuid7-" + UUID.randomUUID() + ";MODE=MariaDB;DATABASE_TO_LOWER=TRUE");
      properties.setProperty("hibernate.connection.username", "sa");
      properties.setProperty("hibernate.connection.password", "");
      properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    }
    properties.setProperty("hibernate.show_sql", "false");
    Configuration configuration = new Configuration();
    configuration.setProperties(properties);
    new EntityScanner().scanAndAddEntities(configuration, "local.jarios.entity");
    return configuration.buildSessionFactory();
  }
}
