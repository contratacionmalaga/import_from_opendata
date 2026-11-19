package local.jarios.mappers.atom;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.time.Instant;
import java.util.List;
import javax.xml.bind.JAXBElement;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.codice.NoticeInfo;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus.Language;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus.ParentLocatedParty;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus.PartyIdentifier;
import local.jarios.helpers.UnmarshallerHelper;
import org.junit.jupiter.api.Test;
import org.w3._2005.atom.FeedType;

class SyndicationMapperRegressionTest {

  @Test
  void conserva_el_identificador_atom_completo_y_el_desplazamiento_original() throws Exception {
    String id = "https://example.test/" + "x".repeat(550) + "/expediente";
    Entry entry = mapearEntrada("Expediente", "", id, "2026-10-25T02:10:00.123456+01:00");

    assertThat(entry.getEntryId()).isEqualTo(id);
    assertThat(entry.getEntryIdCorto()).isEqualTo("expediente");
    assertThat(entry.getUpdatedOffsetSeconds()).isEqualTo(3600);
    assertThat(entry.updatedInstant()).isEqualTo(Instant.parse("2026-10-25T01:10:00.123456Z"));
  }

  @Test
  void conserva_el_titulo_atom_completo_hasta_dos_mil_caracteres() throws Exception {
    String titulo = "Contratación del servicio. ".repeat(100).substring(0, 2000);
    Entry entry = mapearEntrada(titulo, "");

    assertThat(entry.getTitle()).isEqualTo(titulo);
  }

  @Test
  void importa_anuncios_de_consulta_sin_documentos_generales() throws Exception {
    Entry entry =
        mapearEntrada(
            "Consulta preliminar de mercado",
            """
            <ext:PreliminaryMarketConsultationStatus
                xmlns:ext="urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonAggregateComponents-2"
                xmlns:basic="urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonBasicComponents-2">
              <ext:LocatedContractingParty/>
              <ext:ValidNoticeInfo>
                <basic:NoticeTypeCode>DOC_CPM</basic:NoticeTypeCode>
              </ext:ValidNoticeInfo>
            </ext:PreliminaryMarketConsultationStatus>
            """);

    var consulta = entry.getPreliminaryMarketConsultationStatusList().getFirst();
    assertThat(consulta.getGeneralDocumentReferenceList()).isEmpty();
    assertThat(consulta.getValideNoticeInfoList())
        .extracting(NoticeInfo::getNoticeTypeCode)
        .containsExactly("DOC_CPM");
  }

  @Test
  void conserva_la_jerarquia_direccion_idiomas_y_enlace_de_presentacion_de_la_consulta()
      throws Exception {
    Entry entry = mapearEntrada(
        "Consulta preliminar",
        """
        <ext:PreliminaryMarketConsultationStatus
            xmlns:ext="urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonAggregateComponents-2"
            xmlns:cac="urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2"
            xmlns:cbc="urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2">
          <ext:LocatedContractingParty>
            <cbc:BuyerProfileURIID>https://example.test/perfil</cbc:BuyerProfileURIID>
            <cac:Party>
              <cac:PostalAddress>
                <cbc:CityName>Test</cbc:CityName>
                <cbc:PostalZone>01234</cbc:PostalZone>
                <cac:AddressLine><cbc:Line>Calle 1</cbc:Line></cac:AddressLine>
                <cac:AddressLine><cbc:Line>Edificio A</cbc:Line></cac:AddressLine>
                <cac:Country><cbc:IdentificationCode>ES</cbc:IdentificationCode><cbc:Name>España</cbc:Name></cac:Country>
              </cac:PostalAddress>
            </cac:Party>
            <ext:ParentLocatedParty>
              <cac:PartyIdentification><cbc:ID schemeName="DIR3">P42</cbc:ID></cac:PartyIdentification>
              <cac:PartyName><cbc:Name>Padre inmediato</cbc:Name></cac:PartyName>
              <ext:ParentLocatedParty>
                <cac:PartyName><cbc:Name>Raíz</cbc:Name></cac:PartyName>
              </ext:ParentLocatedParty>
            </ext:ParentLocatedParty>
          </ext:LocatedContractingParty>
          <cac:Attachment>
            <cac:ExternalReference><cbc:URI>https://example.test/presentar?a=1&amp;b=2</cbc:URI></cac:ExternalReference>
          </cac:Attachment>
          <cac:Language><cbc:ID>ca-valencia</cbc:ID></cac:Language>
          <cac:Language><cbc:ID>en</cbc:ID><cbc:Name>English</cbc:Name><cbc:LocaleCode>en-GB</cbc:LocaleCode></cac:Language>
        </ext:PreliminaryMarketConsultationStatus>
        """);

    var consultation = entry.getPreliminaryMarketConsultationStatusList().getFirst();
    assertThat(consultation.getParentLocatedParties())
        .containsExactly(
            new ParentLocatedParty(
                List.of("Padre inmediato"), List.of(new PartyIdentifier("DIR3", "P42"))),
            new ParentLocatedParty(List.of("Raíz"), List.of()));
    assertThat(consultation.getPostalAddressLines()).containsExactly("Calle 1", "Edificio A");
    assertThat(consultation.getPostalCityName()).isEqualTo("Test");
    assertThat(consultation.getPostalZone()).isEqualTo("01234");
    assertThat(consultation.getPostalCountryCode()).isEqualTo("ES");
    assertThat(consultation.getPostalCountryName()).isEqualTo("España");
    assertThat(consultation.getLanguages())
        .containsExactly(
            new Language("ca-valencia", null, null), new Language("en", "English", "en-GB"));
    assertThat(consultation.getBuyerProfileUriId()).isEqualTo("https://example.test/perfil");
    assertThat(consultation.getSubmissionUri()).isEqualTo("https://example.test/presentar?a=1&b=2");
  }

  private static Entry mapearEntrada(String titulo, String contenido) throws Exception {
    return mapearEntrada(titulo, contenido, "https://example.test/entrada/1", "2026-11-19T10:00:00Z");
  }

  private static Entry mapearEntrada(String titulo, String contenido, String id, String updated)
      throws Exception {
    String xml =
        """
        <feed xmlns="http://www.w3.org/2005/Atom">
          <id>urn:test:feed</id>
          <title>Feed de prueba</title>
          <updated>2026-11-19T10:00:00Z</updated>
          <link rel="self" href="https://example.test/feed.atom"/>
          <entry>
            <id>%s</id>
            <title>%s</title>
            <summary>Resumen</summary>
            <updated>%s</updated>
            %s
          </entry>
        </feed>
        """
            .formatted(id, titulo, updated, contenido);
    var feed =
        (JAXBElement<?>) UnmarshallerHelper.getUnmarshaller().unmarshal(new StringReader(xml));
    return MapperFeed.getFeed((FeedType) feed.getValue()).getEntryList().getFirst();
  }
}
