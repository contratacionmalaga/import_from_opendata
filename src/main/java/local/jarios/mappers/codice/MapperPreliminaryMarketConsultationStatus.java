package local.jarios.mappers.codice;

import ext.place.codice.common.caclib.LocatedContractingPartyType;
import ext.place.codice.common.caclib.LocatedPartyType;
import ext.place.codice.common.caclib.PreliminaryMarketConsultationStatusType;
import ext.place.codice.common.cbclib.PreliminaryMarketConsultationStatusCodeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.xml.bind.JAXBElement;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.codice.DocumentReference;
import local.jarios.entity.codice.NoticeInfo;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus.Language;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus.ParentLocatedParty;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus.PartyIdentifier;
import local.jarios.enums.TipoDocumento;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AddressLineType;
import org.dgpe.codice.common.caclib.AddressType;
import org.dgpe.codice.common.caclib.AttachmentType;
import org.dgpe.codice.common.caclib.ExternalReferenceType;
import org.dgpe.codice.common.caclib.PartyIdentificationType;
import org.dgpe.codice.common.caclib.PartyNameType;
import org.dgpe.codice.common.caclib.PartyType;
import org.dgpe.codice.common.cbclib.BuyerProfileURIIDType;
import org.dgpe.codice.common.cbclib.CityNameType;
import org.dgpe.codice.common.cbclib.ConditionTypeCodeType;
import org.dgpe.codice.common.cbclib.ConditionsTextType;
import org.dgpe.codice.common.cbclib.ConsultationNameType;
import org.dgpe.codice.common.cbclib.ContractingPartyTypeCodeType;
import org.dgpe.codice.common.cbclib.IDType;
import org.dgpe.codice.common.cbclib.IdentificationCodeType;
import org.dgpe.codice.common.cbclib.LimitDateType;
import org.dgpe.codice.common.cbclib.LineType;
import org.dgpe.codice.common.cbclib.LocaleCodeType;
import org.dgpe.codice.common.cbclib.NameType;
import org.dgpe.codice.common.cbclib.PartySelectionReasonTextType;
import org.dgpe.codice.common.cbclib.PlannedDateType;
import org.dgpe.codice.common.cbclib.PostalZoneType;
import org.dgpe.codice.common.cbclib.PreliminaryMarketConsultationIDType;
import org.dgpe.codice.common.cbclib.TextType;
import org.dgpe.codice.common.cbclib.WebsiteURIType;
import org.w3._2005.atom.EntryType;
import un.unece.uncefact.data.specification.corecomponenttypeschemamodule._2.IdentifierType;

/** Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio. */
@Slf4j
public final class MapperPreliminaryMarketConsultationStatus {

  private MapperPreliminaryMarketConsultationStatus() {
    // Utility class.
  }

  /**
   * @param entry entidad {@link Entry} padre (no nula).
   * @param entryType entrada Atom con contenido mixto (no nula).
   * @return lista inmutable de estados de expediente mapeados.
   */
  public static List<PreliminaryMarketConsultationStatus>
      getListPreliminaryMarketConsultationStatusFromListType(Entry entry, EntryType entryType) {

    if (entryType == null || entryType.getAny() == null || entryType.getAny().isEmpty()) {
      return List.of();
    }

    return entryType.getAny().stream()
        .filter(JAXBElement.class::isInstance)
        .map(JAXBElement.class::cast)
        .map(JAXBElement::getValue)
        .filter(PreliminaryMarketConsultationStatusType.class::isInstance)
        .map(PreliminaryMarketConsultationStatusType.class::cast)
        .map(type -> mapPreliminaryMarketConsultationStatus(entry, type))
        .toList();
  }

  private static PreliminaryMarketConsultationStatus mapPreliminaryMarketConsultationStatus(
      Entry entry, PreliminaryMarketConsultationStatusType type) {

    PreliminaryMarketConsultationStatus pmcs = new PreliminaryMarketConsultationStatus();
    pmcs.setEntry(entry);

    mapFolderBasics(pmcs, type);
    mapLocatedContractingParty(pmcs, type.getLocatedContractingParty());
    mapConsultationLanguagesAndSubmission(pmcs, type);
    mapDocumentReferences(pmcs, type);
    mapValidNoticeInfo(pmcs, type);
    mapProcurement(pmcs, type);
    mapTendering(pmcs, type);

    return pmcs;
  }

  private static void mapFolderBasics(
      PreliminaryMarketConsultationStatus pmcs, PreliminaryMarketConsultationStatusType type) {

    setLimited(
        pmcs::setPreliminaryMarketConsultationStatusCode,
        safeValue(
            type.getPreliminaryMarketConsultationStatusCode(),
            PreliminaryMarketConsultationStatusCodeType::getValue),
        TamanoCampos.TAMANO_50);

    setLimited(
        pmcs::setPreliminaryMarketConsultationID,
        safeValue(
            type.getPreliminaryMarketConsultationID(),
            PreliminaryMarketConsultationIDType::getValue),
        TamanoCampos.TAMANO_50);

    setLimited(
        pmcs::setConsultationName,
        safeValue(type.getConsultationName(), ConsultationNameType::getValue),
        TamanoCampos.TAMANO_2500);

    setLimited(
        pmcs::setConditionTypeCode,
        safeValue(type.getConditionTypeCode(), ConditionTypeCodeType::getValue),
        TamanoCampos.TAMANO_50);

    setLimited(
        pmcs::setConditionsText,
        safeValue(type.getConditionsText(), ConditionsTextType::getValue),
        TamanoCampos.TAMANO_2500);

    setLimited(
        pmcs::setPartySelectionReasonText,
        safeValue(type.getPartySelectionReasonText(), PartySelectionReasonTextType::getValue),
        TamanoCampos.TAMANO_2500);

    setLimited(
        pmcs::setConditionTypeReasonText,
        safeValue(type.getConditionTypeReasonText(), TextType::getValue),
        TamanoCampos.TAMANO_2500);

    Optional.ofNullable(type.getPlannedDate())
        .map(PlannedDateType::getValue)
        .map(xmlDate -> xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDate())
        .ifPresent(pmcs::setPlannedDate);

    Optional.ofNullable(type.getLimitDate())
        .map(LimitDateType::getValue)
        .map(xmlDate -> xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDate())
        .ifPresent(pmcs::setLimitDate);
  }

  private static void mapLocatedContractingParty(
      PreliminaryMarketConsultationStatus pmcs, LocatedContractingPartyType type) {

    if (type == null) {
      return;
    }

    setLimited(
        pmcs::setBuyerProfileUriId,
        safeValue(type.getBuyerProfileURIID(), BuyerProfileURIIDType::getValue),
        TamanoCampos.TAMANO_500);

    setLimited(
        pmcs::setContractingPartyTypeCode,
        safeValue(type.getContractingPartyTypeCode(), ContractingPartyTypeCodeType::getValue),
        TamanoCampos.TAMANO_500);

    mapParty(pmcs, type.getParty());
    pmcs.setParentLocatedParties(mapParentLocatedParties(type.getParentLocatedParty()));
  }

  private static void mapParty(PreliminaryMarketConsultationStatus entity, PartyType type) {

    if (type == null) {
      return;
    }

    setLimited(
        entity::setWebSiteUri,
        safeValue(type.getWebsiteURI(), WebsiteURIType::getValue),
        TamanoCampos.TAMANO_500);

    // Según tu contexto: listas no nulas (como mínimo vacías).
    entity.setPartyName(MapperStringFromList.getStringFromListPartyNameType(type.getPartyName()));

    mapPartyIdentifications(entity, type.getPartyIdentification());
    mapPostalAddress(entity, type.getPostalAddress());
  }

  private static ParentLocatedParty[] mapParentLocatedParties(LocatedPartyType parent) {
    List<ParentLocatedParty> parents = new ArrayList<>();
    for (LocatedPartyType current = parent;
        current != null;
        current = current.getParentLocatedParty()) {
      List<String> names =
          current.getPartyName().stream()
              .map(PartyNameType::getName)
              .filter(Objects::nonNull)
              .map(NameType::getValue)
              .toList();
      List<PartyIdentifier> identifiers =
          current.getPartyIdentification().stream()
              .map(PartyIdentificationType::getID)
              .filter(Objects::nonNull)
              .map(id -> new PartyIdentifier(id.getSchemeName(), id.getValue()))
              .toList();
      parents.add(new ParentLocatedParty(names, identifiers));
    }
    return parents.toArray(ParentLocatedParty[]::new);
  }

  private static void mapPostalAddress(PreliminaryMarketConsultationStatus pmcs, AddressType address) {
    if (address == null) {
      return;
    }

    pmcs.setPostalAddressLines(
        address.getAddressLine().stream()
            .map(AddressLineType::getLine)
            .filter(Objects::nonNull)
            .map(LineType::getValue)
            .toArray(String[]::new));
    pmcs.setPostalCityName(safeValue(address.getCityName(), CityNameType::getValue));
    setLimited(
        pmcs::setPostalZone,
        safeValue(address.getPostalZone(), PostalZoneType::getValue),
        TamanoCampos.TAMANO_50);
    if (address.getCountry() != null) {
      var country = address.getCountry();
      setLimited(
          pmcs::setPostalCountryCode,
          safeValue(country.getIdentificationCode(), IdentificationCodeType::getValue),
          TamanoCampos.TAMANO_50);
      pmcs.setPostalCountryName(safeValue(country.getName(), NameType::getValue));
    }
  }

  private static void mapConsultationLanguagesAndSubmission(
      PreliminaryMarketConsultationStatus pmcs, PreliminaryMarketConsultationStatusType type) {
    pmcs.setLanguages(
        type.getLanguage().stream()
            .map(
                language ->
                    new Language(
                        safeValue(language.getID(), IDType::getValue),
                        safeValue(language.getName(), NameType::getValue),
                        safeValue(language.getLocaleCode(), LocaleCodeType::getValue)))
            .toArray(Language[]::new));

    Optional.ofNullable(type.getAttachment())
        .map(AttachmentType::getExternalReference)
        .map(ExternalReferenceType::getURI)
        .map(IdentifierType::getValue)
        .ifPresent(pmcs::setSubmissionUri);
  }

  private static void mapPartyIdentifications(
      PreliminaryMarketConsultationStatus pmcs, List<PartyIdentificationType> identifications) {

    // Según tu contexto: lista no nula.
    for (PartyIdentificationType identification : identifications) {
      applyPartyIdentification(pmcs, identification);
    }
  }

  private static void applyPartyIdentification(
      PreliminaryMarketConsultationStatus pmcs, PartyIdentificationType identification) {

    if (identification == null || identification.getID() == null) {
      return;
    }

    String rawId = identification.getID().getValue();
    String schemeName = identification.getID().getSchemeName();
    if (rawId == null || schemeName == null) {
      return;
    }

    String limitedId = StringHelper.limit(rawId, TamanoCampos.TAMANO_50);

    switch (schemeName) {
      case Constantes.DIR3 -> pmcs.setDir3(limitedId);
      case Constantes.IDPLATAFORMA -> pmcs.setIdPlataforma(limitedId);
      case Constantes.NIF -> pmcs.setNif(limitedId);
      default -> {
        // Esquema desconocido: intencionalmente ignorado.
      }
    }
  }

  private static void mapDocumentReferences(
      PreliminaryMarketConsultationStatus pmcs, PreliminaryMarketConsultationStatusType type) {

    if (!type.getGeneralDocument().isEmpty()) {
      List<DocumentReference> list =
          MapperGeneralDocument.getListGeneralDocumentFromType(
              null, pmcs, TipoDocumento.GENERAL, type.getGeneralDocument());
      pmcs.setGeneralDocumentReferenceList(list);
    }
  }

  private static void mapValidNoticeInfo(
      PreliminaryMarketConsultationStatus pmcs, PreliminaryMarketConsultationStatusType type) {

    if (!type.getValidNoticeInfo().isEmpty()) {
      List<NoticeInfo> list =
          MapperNoticeInfo.getListNoticeInfoFromType(null, pmcs, type.getValidNoticeInfo());
      pmcs.setValideNoticeInfoList(list);
    }
  }

  private static void mapProcurement(
      PreliminaryMarketConsultationStatus pmcs, PreliminaryMarketConsultationStatusType type) {

    pmcs.setProcurementProject(
        MapperProcurementProject.getProcurementProjectFromType(
            null, pmcs, type.getProcurementProject()));
  }

  private static void mapTendering(
      PreliminaryMarketConsultationStatus pmcs, PreliminaryMarketConsultationStatusType type) {

    pmcs.setTenderingProcess(
        MapperTenderingProcess.getTenderingProcessFromType(null, pmcs, type.getTenderingProcess()));
  }

  private static <T> String safeValue(T obj, java.util.function.Function<T, String> mapper) {
    if (obj == null) {
      return null;
    }
    return mapper.apply(obj);
  }

  private static void setLimited(
      java.util.function.Consumer<String> setter, String value, int maxLength) {

    if (value == null) {
      return;
    }
    setter.accept(StringHelper.limit(value, maxLength));
  }
}
