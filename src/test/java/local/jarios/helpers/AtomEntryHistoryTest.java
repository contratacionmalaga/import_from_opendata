package local.jarios.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.enums.EntryOpcion;
import local.jarios.exceptions.MiParseException;
import local.jarios.interfaces.FeedSource;
import local.jarios.repositories.EntrySnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AtomEntryHistoryTest {

  @ParameterizedTest
  @CsvSource({"true, false", "true, true", "false, false", "false, true"})
  void solo_la_version_que_se_persiste_lleva_la_accion_de_escritura(
      boolean aplicarFiltros, boolean existeEnBd) throws Exception {
    var context = malaga();
    context.setAplicarFiltros(aplicarFiltros);
    context.setCompararConExistentes(true);
    if (existeEnBd) {
      context
          .getMapEntrySnapshotsFromBaseDatos()
          .put(
              "urn:test:42",
              new EntrySnapshot("urn:test:42", LocalDateTime.parse("2026-10-09T10:00:00")));
    }
    String first = feed(entry("42", 10, "P2900000G", "candidato provisional"));
    String second =
        feed(entry("42", 11, "P2900000G", "candidato definitivo"))
            .replace("<link rel=\"next\" href=\"https://example.test/otro.atom\"/>", "");
    FeedHelper.parsearFeeds(
        new Pages(Map.of("first", first, "https://example.test/otro.atom", second)), context);

    assertThat(context.getListHistoricos())
        .satisfiesExactly(
            previous -> {
              assertThat(previous.getEntryOpcion()).isEqualTo(EntryOpcion.RECHAZAR);
              assertThat(previous.getEntryMotivo()).contains("antes de persistir");
              assertThat(previous.getXml()).contains("candidato provisional");
            },
            selected -> {
              assertThat(selected.getEntryOpcion())
                  .isEqualTo(existeEnBd ? EntryOpcion.ACTUALIZAR : EntryOpcion.INSERTAR);
              assertThat(selected.getXml()).contains("candidato definitivo");
              assertThat(selected.getXml())
                  .isEqualTo(context.getMapEntriesFromAtoms().get("urn:test:42").getXml());
            });
  }

  @Test
  void conserva_las_dos_versiones_y_solo_la_mas_reciente_como_vigente() throws Exception {
    OpenDataExecutionContext context = malaga();
    context.setCompararConExistentes(true);

    String feed =
        feed(
            entry("42", 10, "P2900000G", "documento anterior")
                + entry("42", 11, "P2900000G", "documento vigente")
                + entry("43", 9, "P9999999Z", "fuera de Málaga"));
    FeedHelper.parsearFeeds(new OneFeed(feed), context);

    assertThat(context.getMapEntriesFromAtoms()).containsOnlyKeys("urn:test:42");
    Entry vigente = context.getMapEntriesFromAtoms().get("urn:test:42");
    assertThat(vigente.getXml()).contains("documento vigente", "xmlns:doc=\"urn:test:documento\"");
    assertThat(context.getListHistoricos())
        .satisfiesExactly(
            actual -> {
              assertThat(actual.getEntryOpcion()).isEqualTo(EntryOpcion.INSERTAR);
              assertThat(actual.getXml()).isEqualTo(vigente.getXml());
              assertThat(actual.getSummaryExpediente()).isEqualTo("C.28;G489/2026");
              assertThat(actual.getSummaryOrganoContratacion()).isEqualTo("Ayuntamiento de Málaga");
              assertThat(actual.getSummaryImporte()).isEqualTo("23500 EUR");
              assertThat(actual.getSummaryEstado()).isEqualTo("EV");
            },
            anterior -> {
              assertThat(anterior.getEntryOpcion()).isEqualTo(EntryOpcion.RECHAZAR);
              assertThat(anterior.getEntryMotivo()).contains("ANTERIOR");
              assertThat(anterior.getXml()).contains("documento anterior");
            });
  }

  @Test
  void pliegos_archiva_el_xml_de_cada_version_incluso_si_se_rechaza() throws Exception {
    var context = new OpenDataExecutionContext();
    context.setTipoSindicacion(TipoSindicacion.MAYORES);
    FeedHelper.parsearFeeds(
        new OneFeed(
            feed(
                entry("42", 10, "P2900000G", "documento anterior")
                    + entry("42", 11, "P2900000G", "documento vigente"))),
        context);

    assertThat(context.getMapEntriesFromAtoms()).containsOnlyKeys("urn:test:42");
    assertThat(context.getMapEntriesFromAtoms().get("urn:test:42").getXml())
        .contains("<doc:extra>documento vigente</doc:extra>", "xmlns:doc=\"urn:test:documento\"")
        .doesNotContain("documento anterior");
    assertThat(context.getListHistoricos())
        .satisfiesExactly(
            alta -> {
              assertThat(alta.getEntryOpcion()).isEqualTo(EntryOpcion.INSERTAR);
              assertThat(alta.getXml())
                  .isEqualTo(context.getMapEntriesFromAtoms().get("urn:test:42").getXml());
              assertThat(alta.getSummaryExpediente()).isEqualTo("C.28;G489/2026");
            },
            anterior -> {
              assertThat(anterior.getEntryOpcion()).isEqualTo(EntryOpcion.RECHAZAR);
              assertThat(anterior.getXml()).contains("documento anterior");
            });
    assertThat(context.getHistoricosRechazadosOmitidos()).isZero();
  }

  @Test
  void conserva_el_historico_de_malaga_aunque_ninguna_entrada_pase_el_filtro_de_fechas()
      throws Exception {
    OpenDataExecutionContext context = malaga();
    context.setFiltroFechaFinal(LocalDateTime.parse("2026-10-11T00:00:00"));
    FeedHelper.parsearFeeds(
        new OneFeed(feed(entry("42", 10, "P2900000G", "documento anterior"))), context);

    assertThat(context.getMapEntriesFromAtoms()).isEmpty();
    assertThat(context.getListHistoricos())
        .singleElement()
        .satisfies(
            original -> {
              assertThat(original.getEntryOpcion()).isEqualTo(EntryOpcion.RECHAZAR);
              assertThat(original.getEntryMotivo()).contains("FiltroFechasEvaluator");
              assertThat(original.getXml()).contains("documento anterior");
            });
  }

  @Test
  void conserva_los_historicos_de_las_versiones_que_comparten_fecha_en_la_importacion_local()
      throws Exception {
    OpenDataExecutionContext context = malaga();
    FeedHelper.parsearFeeds(
        new OneFeed(
            feed(
                entry("42", 10, "P2900000G", "primera publicación")
                    + entry("42", 10, "P2900000G", "segunda publicación"))),
        context);

    assertThat(context.getListHistoricos())
        .extracting(HistoricoEntry::getXml)
        .satisfiesExactly(
            xml -> assertThat(xml).contains("primera publicación"),
            xml -> assertThat(xml).contains("segunda publicación"));
    assertThat(context.getMapEntriesFromAtoms().get("urn:test:42").getXml())
        .contains("primera publicación");
  }

  @Test
  void el_filtro_usa_el_nif_del_organo_con_independencia_del_orden_de_otros() throws Exception {
    OpenDataExecutionContext context = malaga();
    String nif = identification("NIF", "P2900000G");
    String otros = identification("OTROS", "identificador adicional");
    FeedHelper.parsearFeeds(
        new OneFeed(
            feed(
                entryWithIdentifications("42", 10, nif + otros, "NIF primero")
                    + entryWithIdentifications("43", 10, otros + nif, "OTROS primero"))),
        context);

    assertThat(context.getMapEntriesFromAtoms()).containsOnlyKeys("urn:test:42", "urn:test:43");
    assertThat(context.getMapEntriesFromAtoms().values())
        .allSatisfy(
            entry -> {
              assertThat(entry.getContractFolderStatusList())
                  .singleElement()
                  .satisfies(
                      status -> {
                        assertThat(status.getNif()).isEqualTo("P2900000G");
                        assertThat(status.getOtros()).isEqualTo("identificador adicional");
                      });
              assertThat(entry.getXml()).contains("P2900000G", "identificador adicional");
            });
    assertThat(context.getListHistoricos())
        .hasSize(2)
        .allSatisfy(history -> assertThat(history.getNif()).isEqualTo("P2900000G"));
  }

  @Test
  void otros_no_sustituye_a_un_nif_ausente_o_ajeno_al_filtro() throws Exception {
    OpenDataExecutionContext context = malaga();
    String otros = identification("OTROS", "P2900000G");
    String nifFueraDelFiltro = identification("NIF", "P9999999Z");
    FeedHelper.parsearFeeds(
        new OneFeed(
            feed(
                entryWithIdentifications("42", 10, otros, "sin NIF")
                    + entryWithIdentifications("43", 10, nifFueraDelFiltro + otros, "NIF ajeno"))),
        context);

    assertThat(context.getMapEntriesFromAtoms()).isEmpty();
  }

  @Test
  void deja_sin_importe_el_resumen_de_consultas() {
    assertThat(
            AtomSummary.parse(
                "Id consulta: CPM-42; Órgano de Contratación: Diputación; Estado: PUB"))
        .isEqualTo(new AtomSummary("CPM-42", "Diputación", null, "PUB"));
  }

  @Test
  void rechaza_el_doctype_en_el_parser_del_historico() {
    String unsafe = "<!DOCTYPE feed SYSTEM 'file:///not-allowed'>" + feed("");
    assertThatThrownBy(() -> FeedHelper.parsearFeeds(new OneFeed(unsafe), malaga()))
        .isInstanceOf(MiParseException.class);
  }

  private static OpenDataExecutionContext malaga() {
    var context = new OpenDataExecutionContext();
    context.setAplicarFiltros(true);
    context.setTipoSindicacion(TipoSindicacion.MAYORES);
    context.setFiltroFechaInicial(LocalDateTime.parse("2026-10-31T00:00:00"));
    context.setFiltroFechaFinal(LocalDateTime.parse("2026-10-01T00:00:00"));
    context.setConjuntoNifsEnFiltro(Set.of("P2900000G"));
    return context;
  }

  private static String feed(String entries) {
    return """
        <feed xmlns="http://www.w3.org/2005/Atom"
              xmlns:ext="urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonAggregateComponents-2"
              xmlns:cac="urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2"
              xmlns:cbc="urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2"
              xmlns:doc="urn:test:documento">
          <id>https://example.test/feed</id>
          <title>Feed de prueba</title>
          <updated>2026-10-11T10:00:00+02:00</updated>
          <link rel="self" href="https://example.test/feed.atom"/>
          <link rel="next" href="https://example.test/otro.atom"/>
          %s
        </feed>
        """
        .formatted(entries);
  }

  private static String entry(String id, int day, String nif, String marker) {
    return entryWithIdentifications(id, day, identification("NIF", nif), marker);
  }

  private static String identification(String scheme, String value) {
    return """
        <cac:PartyIdentification><cbc:ID schemeName="%s">%s</cbc:ID></cac:PartyIdentification>
        """
        .formatted(scheme, value);
  }

  private static String entryWithIdentifications(
      String id, int day, String identifications, String marker) {
    return """
        <entry>
          <id>urn:test:%s</id><title>Expediente de prueba</title>
          <summary>Id licitación: C.28;G489/2026; Órgano de Contratación: Ayuntamiento de Málaga; Importe: 23500 EUR; Estado: EV</summary>
          <updated>2026-10-%02dT10:00:00+02:00</updated>
          <doc:extra>%s</doc:extra>
          <ext:ContractFolderStatus>
            <ext:LocatedContractingParty><cac:Party>
              %s
            </cac:Party></ext:LocatedContractingParty>
          </ext:ContractFolderStatus>
        </entry>
        """
        .formatted(id, day, marker, identifications);
  }

  private record Pages(Map<String, String> pages) implements FeedSource {
    @Override
    public String getInitialLink() {
      return "first";
    }

    @Override
    public boolean isNextLinkValid(String link) {
      return pages.containsKey(link);
    }

    @Override
    public String getNextLink(Feed feed) {
      return feed.getLinkNext();
    }

    @Override
    public BufferedReader openBufferedReader(String path) {
      return new BufferedReader(new StringReader(pages.get(path)));
    }
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
      assertThat(path).isEqualTo("first");
      return new BufferedReader(new StringReader(xml));
    }
  }
}
