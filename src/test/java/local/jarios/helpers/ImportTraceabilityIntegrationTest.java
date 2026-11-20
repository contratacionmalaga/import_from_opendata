package local.jarios.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.database.EntityScanner;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.entity.auxiliares.Log;
import local.jarios.enums.EntryOpcion;
import local.jarios.exceptions.MiRepositoryException;
import local.jarios.interfaces.FeedSource;
import local.jarios.repositories.RepositoryImpl;
import local.jarios.services.ImportPersistencePlan;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Comprueba los XML y su procedencia después de confirmar varias importaciones reales del
 * repositorio.
 */
class ImportTraceabilityIntegrationTest {

  @ParameterizedTest
  @EnumSource(
      value = TipoSindicacion.class,
      names = {"MAYORES", "MENORES", "ENCARGOS", "CONSULTAS"})
  void conserva_ayer_y_hoy_con_sus_xml_fechas_y_ejecuciones_y_no_toca_otro_expediente(
      TipoSindicacion tipo) throws Exception {
    try (SessionFactory factory = newSessionFactory()) {
      var repository = new RepositoryImpl(factory);
      var yesterday =
          parse(
              repository,
              tipo,
              19,
              entry(tipo, "42", 19, "ayer") + entry(tipo, "43", 19, "sin-cambios"));
      String yesterdayXml = yesterday.getMapEntriesFromAtoms().get("urn:test:42").getXml();
      Log yesterdayLog = persist(repository, yesterday);
      UUID originalId = current(factory, "42").getId();
      String untouchedXml = current(factory, "43").getXml();
      LocalDateTime firstCreated = current(factory, "42").getCreatedAt();

      var today = parse(repository, tipo, 20, entry(tipo, "42", 20, "hoy"));
      String todayXml = today.getMapEntriesFromAtoms().get("urn:test:42").getXml();
      Log todayLog = persist(repository, today);

      assertThat(current(factory, "42").getId()).isEqualTo(originalId);
      assertThat(current(factory, "42").getCreatedAt()).isEqualTo(firstCreated);
      assertThat(current(factory, "42").getXml()).isEqualTo(todayXml);
      assertThat(current(factory, "43").getXml()).isEqualTo(untouchedXml);
      List<HistoricoEntry> versions = histories(factory, "42");
      assertThat(versions).hasSize(2);
      assertThat(versions.get(0).getXml()).isEqualTo(yesterdayXml).contains("ayer", "xmlns:doc=");
      assertThat(versions.get(1).getXml()).isEqualTo(todayXml).contains("hoy", "xmlns:doc=");
      assertThat(versions.get(0).getEntryOpcion()).isEqualTo(EntryOpcion.INSERTAR);
      assertThat(versions.get(1).getEntryOpcion()).isEqualTo(EntryOpcion.ACTUALIZAR);
      assertThat(versions.get(0).getEntryUpdated()).isEqualTo(sourceDate(19));
      assertThat(versions.get(1).getEntryUpdated()).isEqualTo(sourceDate(20));
      assertThat(versions)
          .allSatisfy(
              h -> {
                assertThat(h.getEntryUpdatedOffsetSeconds()).isEqualTo(3600);
                assertThat(h.getEntryMotivo()).isNotBlank();
                assertThat(h.getMiLog().getCreatedAt()).isNotNull();
                assertThat(h.getMiLog().getTipoSindicacion()).isEqualTo(tipo);
              });
      assertThat(versions.get(0).getMiLog().getId()).isEqualTo(yesterdayLog.getId());
      assertThat(versions.get(1).getMiLog().getId()).isEqualTo(todayLog.getId());
      assertThat(todayLog.getId()).isNotEqualTo(yesterdayLog.getId());
      assertThat(count(factory, "from Configuracion")).isEqualTo(2);
      assertThat(count(factory, "from Estadistica")).isEqualTo(2);

      // Repetir el origen registra otra observación sin alterar la versión vigente.
      persist(repository, parse(repository, tipo, 20, entry(tipo, "42", 20, "hoy")));
      assertThat(histories(factory, "42")).hasSize(3);
      assertThat(histories(factory, "42").getLast().getEntryOpcion())
          .isEqualTo(EntryOpcion.RECHAZAR);
      assertThat(current(factory, "42").getXml()).isEqualTo(todayXml);

      // Incluso si el proveedor cambia el contenido sin cambiar updated, el nuevo XML se archiva.
      persist(
          repository, parse(repository, tipo, 20, entry(tipo, "42", 20, "mismo-updated-otro-xml")));
      assertThat(histories(factory, "42")).hasSize(4);
      assertThat(histories(factory, "42").getLast().getXml()).contains("mismo-updated-otro-xml");
      assertThat(current(factory, "42").getXml()).isEqualTo(todayXml);

      // Una versión fuera del rango de hoy se conserva en el histórico del ámbito Málaga.
      persist(repository, parse(repository, tipo, 20, entry(tipo, "42", 19, "ayer-reobservado")));
      assertThat(histories(factory, "42")).hasSize(5);
      assertThat(histories(factory, "42").getLast().getXml()).contains("ayer-reobservado");
      assertThat(histories(factory, "42").getLast().getEntryMotivo())
          .contains("FiltroFechasEvaluator");
      assertThat(current(factory, "42").getXml()).isEqualTo(todayXml);
      assertThat(histories(factory, "42").getFirst().getXml()).isEqualTo(yesterdayXml);
      assertThat(count(factory, "from Entry")).isEqualTo(2);

      // Un fallo de persistencia revierte tanto el cambio de vigente como el histórico y el log.
      var failing = parse(repository, tipo, 21, entry(tipo, "42", 21, "no-confirmado"));
      failing.getMapEntriesFromAtoms().get("urn:test:42").setTitle("x".repeat(2501));
      long logsBefore = count(factory, "from Log");
      assertThatThrownBy(() -> persist(repository, failing))
          .isInstanceOf(MiRepositoryException.class);
      assertThat(current(factory, "42").getXml()).isEqualTo(todayXml);
      assertThat(histories(factory, "42")).hasSize(5);
      assertThat(count(factory, "from Log")).isEqualTo(logsBefore);
      assertThat(count(factory, "from Configuracion")).isEqualTo(logsBefore);
      assertThat(count(factory, "from Estadistica")).isEqualTo(logsBefore);
    }
  }

  private static OpenDataExecutionContext parse(
      RepositoryImpl repository, TipoSindicacion tipo, int day, String entries) throws Exception {
    var context = new OpenDataExecutionContext();
    context.setTipoSindicacion(tipo);
    context.setLugarImportacion(LugarImportacion.INTERNET);
    context.setAplicarFiltros(true);
    context.setCompararConExistentes(true);
    context.setPersistirHistoricosRechazados(false);
    context.setConjuntoNifsEnFiltro(Set.of("P2900000G"));
    context.setFiltroCodigosPostales("29");
    context.setFiltroFechaFinal(sourceDate(day).toLocalDate().atStartOfDay());
    context.setFiltroFechaInicial(sourceDate(day).toLocalDate().plusDays(1).atStartOfDay());
    context.getMapEntrySnapshotsFromBaseDatos().putAll(repository.getEntrySnapshots(tipo));
    FeedHelper.parsearFeeds(new OneFeed(feed(entries)), context);
    for (Entry entry : context.getMapEntriesFromAtoms().values()) {
      entry.getFeed().getEntryList().add(entry);
    }
    return context;
  }

  private static Log persist(RepositoryImpl repository, OpenDataExecutionContext context)
      throws Exception {
    var log = new Log(LugarImportacion.INTERNET, context.getTipoSindicacion());
    repository.persistirImportacion(
        new ImportPersistencePlan(
            log,
            new Configuracion(log, context),
            List.of(),
            List.of(),
            context.getConjuntoFeedsFromAtoms(),
            context.getListHistoricos(),
            new Estadistica(log)));
    return log;
  }

  private static Entry current(SessionFactory factory, String id) {
    return factory.fromTransaction(
        session ->
            session
                .createQuery("from Entry where entryId = :id", Entry.class)
                .setParameter("id", "urn:test:" + id)
                .getSingleResult());
  }

  private static List<HistoricoEntry> histories(SessionFactory factory, String id) {
    return factory.fromTransaction(
        session ->
            session
                .createQuery(
                    "from HistoricoEntry h join fetch h.miLog where h.entryId = :id order by h.miLog.createdAt, h.id",
                    HistoricoEntry.class)
                .setParameter("id", "urn:test:" + id)
                .getResultList());
  }

  private static long count(SessionFactory factory, String from) {
    return factory.fromTransaction(
        session -> session.createQuery("select count(*) " + from, Long.class).getSingleResult());
  }

  private static LocalDateTime sourceDate(int day) {
    return LocalDateTime.of(2026, 11, day, 10, 0);
  }

  private static SessionFactory newSessionFactory() throws Exception {
    var properties = new Properties();
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
    properties.setProperty("hibernate.jdbc.batch_size", "20");
    String verificationUrl = System.getProperty("opendata.traceability.mariadb.url");
    if (verificationUrl != null) {
      if (!verificationUrl.matches("jdbc:mariadb://localhost:3306/traceability_check_[a-f0-9]+")) {
        throw new IllegalArgumentException("Solo se permite un esquema temporal de trazabilidad");
      }
      var credentials = new Properties();
      try (var input = Files.newInputStream(Path.of("properties/bd.properties"))) {
        credentials.load(input);
      }
      properties.clear();
      properties.setProperty("jakarta.persistence.jdbc.url", verificationUrl);
      properties.setProperty(
          "jakarta.persistence.jdbc.user",
          credentials.getProperty("jakarta.persistence.jdbc.user"));
      properties.setProperty(
          "jakarta.persistence.jdbc.password",
          credentials.getProperty("jakarta.persistence.jdbc.password"));
      properties.setProperty("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
      properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
      properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
      properties.setProperty("hibernate.jdbc.batch_size", "20");
    }
    var configuration = new Configuration();
    configuration.setProperties(properties);
    new EntityScanner().scanAndAddEntities(configuration, "local.jarios.entity");
    return configuration.buildSessionFactory();
  }

  private static String feed(String entries) {
    return """
        <feed xmlns="http://www.w3.org/2005/Atom"
          xmlns:ext="urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonAggregateComponents-2"
          xmlns:extcbc="urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonBasicComponents-2"
          xmlns:cac="urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2"
          xmlns:cbc="urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2"
          xmlns:doc="urn:test:documento">
          <id>https://example.test/feed</id><title>Feed de trazabilidad</title>
          <updated>2026-11-21T10:00:00+01:00</updated>
          <link rel="self" href="https://example.test/feed.atom"/>
          %s
        </feed>
        """
        .formatted(entries);
  }

  private static String entry(TipoSindicacion tipo, String id, int day, String marker) {
    String status =
        tipo == TipoSindicacion.CONSULTAS
            ? """
          <ext:PreliminaryMarketConsultationStatus>
            <cbc:PreliminaryMarketConsultationID>CPM-%s</cbc:PreliminaryMarketConsultationID>
            <cbc:ConsultationName>Consulta de prueba</cbc:ConsultationName>
            <cbc:ConditionTypeCode>CPM</cbc:ConditionTypeCode>
            %s
          </ext:PreliminaryMarketConsultationStatus>
          """
                .formatted(id, party())
            : """
          <ext:ContractFolderStatus>
            <cbc:ContractFolderID>EXP-%s</cbc:ContractFolderID>
            <extcbc:ContractFolderStatusCode>PUB</extcbc:ContractFolderStatusCode>
            %s
          </ext:ContractFolderStatus>
          """
                .formatted(id, party());
    return """
        <entry><id>urn:test:%s</id><title>Expediente %s</title>
          <link href="https://example.test/expediente/%s"/>
          <summary>Id licitación: EXP-%s; Órgano de Contratación: Málaga; Estado: PUB</summary>
          <updated>2026-11-%02dT10:00:00+01:00</updated>
          <doc:extra>%s</doc:extra>%s
        </entry>
        """
        .formatted(id, marker, id, id, day, marker, status);
  }

  private static String party() {
    return """
        <ext:LocatedContractingParty><cac:Party>
          <cac:PartyIdentification><cbc:ID schemeName="NIF">P2900000G</cbc:ID></cac:PartyIdentification>
        </cac:Party></ext:LocatedContractingParty>
        <cac:ProcurementProject><cbc:Name>Proyecto de prueba</cbc:Name></cac:ProcurementProject>
        """;
  }

  private record OneFeed(String xml) implements FeedSource {
    @Override
    public String getInitialLink() {
      return "first";
    }

    @Override
    public boolean isNextLinkValid(String link) {
      return "first".equals(link);
    }

    @Override
    public String getNextLink(Feed feed) {
      return feed.getLinkNext();
    }

    @Override
    public boolean isMissingNextLinkTerminal(String link) {
      return true;
    }

    @Override
    public BufferedReader openBufferedReader(String path) {
      return new BufferedReader(new StringReader(xml));
    }
  }
}
