package local.jarios.mappers.codice;

import ext.place.codice.common.caclib.ContractFolderStatusType;
import ext.place.codice.common.caclib.LocatedContractingPartyType;
import ext.place.codice.common.cbclib.ContractFolderStatusCodeType;
import java.util.List;
import javax.xml.bind.JAXBElement;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.DocumentReference;
import local.jarios.entity.codice.TenderingTerms;
import local.jarios.enums.TipoDocumento;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import org.dgpe.codice.common.caclib.AddressType;
import org.dgpe.codice.common.caclib.ContactType;
import org.dgpe.codice.common.caclib.DocumentReferenceType;
import org.dgpe.codice.common.caclib.PartyIdentificationType;
import org.dgpe.codice.common.caclib.PartyType;
import org.dgpe.codice.common.caclib.TenderingTermsType;
import org.dgpe.codice.common.cbclib.BuyerProfileURIIDType;
import org.dgpe.codice.common.cbclib.CityNameType;
import org.dgpe.codice.common.cbclib.ContractFolderIDType;
import org.dgpe.codice.common.cbclib.ContractingPartyTypeCodeType;
import org.dgpe.codice.common.cbclib.ElectronicMailType;
import org.dgpe.codice.common.cbclib.NameType;
import org.dgpe.codice.common.cbclib.PostalZoneType;
import org.dgpe.codice.common.cbclib.TelephoneType;
import org.dgpe.codice.common.cbclib.WebsiteURIType;
import org.w3._2005.atom.EntryType;

/**
 * Mapeador de {@link ContractFolderStatusType} a entidad {@link ContractFolderStatus}.
 *
 * <p>Responsabilidad: transformar el modelo JAXB (CÓDICE) a entidades JPA de forma robusta,
 * aplicando límites de longitud según {@link Constantes}.
 */
public final class MapperContractFolderStatus {

  /** Constructor privado para evitar instanciación. */
  private MapperContractFolderStatus() {
    // Utility class.
  }

  /**
   * @param entry entidad {@link Entry} padre (no nula).
   * @param entryType entrada Atom con contenido mixto (no nula).
   * @return lista inmutable de estados de expediente mapeados.
   */
  public static List<ContractFolderStatus> getListContractFolderStatusFromListType(
      Entry entry, EntryType entryType) {

    if (entryType == null || entryType.getAny() == null || entryType.getAny().isEmpty()) {
      return new java.util.ArrayList<>();
    }

    return entryType.getAny().stream()
        .map(JAXBElement.class::cast)
        .filter(elem -> elem.getDeclaredType().equals(ContractFolderStatusType.class))
        .map(MapperContractFolderStatus::castToContractFolderStatus)
        .map(type -> mapContractFolderStatus(entry, type))
        .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
  }

  @SuppressWarnings("unchecked")
  private static ContractFolderStatusType castToContractFolderStatus(JAXBElement<?> element) {
    // Seguro por el filtro previo del declaredType.
    return ((JAXBElement<ContractFolderStatusType>) element).getValue();
  }

  private static ContractFolderStatus mapContractFolderStatus(
      Entry entry, ContractFolderStatusType type) {
    ContractFolderStatus entity = new ContractFolderStatus();
    entity.setEntry(entry);

    mapFolderBasics(entity, type);
    mapLocatedContractingParty(entity, type.getLocatedContractingParty());
    mapDocumentReferences(entity, type);
    mapProcurement(entity, type);
    mapTendering(entity, type);
    mapUuid(entity, type);

    return entity;
  }

  private static void mapFolderBasics(ContractFolderStatus cfs, ContractFolderStatusType type) {
    setLimited(
        cfs::setContractFolderStatusCode,
        safeValue(type.getContractFolderStatusCode(), ContractFolderStatusCodeType::getValue),
        TamanoCampos.TAMANO_50);

    setLimited(
        cfs::setContractFolderId,
        safeValue(type.getContractFolderID(), ContractFolderIDType::getValue),
        TamanoCampos.TAMANO_50);
  }

  private static void mapLocatedContractingParty(
      ContractFolderStatus entity, LocatedContractingPartyType located) {

    if (located == null) {
      return;
    }

    setLimited(
        entity::setBuyerProfileUriId,
        safeValue(located.getBuyerProfileURIID(), BuyerProfileURIIDType::getValue),
        TamanoCampos.TAMANO_500);

    setLimited(
        entity::setContractingPartyTypeCode,
        safeValue(located.getContractingPartyTypeCode(), ContractingPartyTypeCodeType::getValue),
        TamanoCampos.TAMANO_50);

    mapParty(entity, located.getParty());
  }

  private static void mapParty(ContractFolderStatus entity, PartyType party) {
    if (party == null) {
      return;
    }

    setLimited(
        entity::setWebSiteUri,
        safeValue(party.getWebsiteURI(), WebsiteURIType::getValue),
        TamanoCampos.TAMANO_500);

    // Según tu contexto: listas no nulas (como mínimo vacías).
    entity.setPartyName(
        StringHelper.limit(
            MapperStringFromList.getStringFromListPartyNameType(party.getPartyName()),
            TamanoCampos.TAMANO_500));

    mapAddress(entity, party.getPostalAddress());
    mapContact(entity, party.getContact());
    mapPartyIdentifications(entity, party.getPartyIdentification());
  }

  private static void mapAddress(ContractFolderStatus entity, AddressType address) {
    if (address == null) {
      return;
    }

    entity.setAddressLine(
        StringHelper.limit(
            MapperStringFromList.getStringFromListAddressLineType(address.getAddressLine()),
            TamanoCampos.TAMANO_2500));

    setLimited(
        entity::setCityName,
        safeValue(address.getCityName(), CityNameType::getValue),
        TamanoCampos.TAMANO_500);

    setLimited(
        entity::setPostalZone,
        safeValue(address.getPostalZone(), PostalZoneType::getValue),
        TamanoCampos.TAMANO_500);
  }

  private static void mapContact(ContractFolderStatus entity, ContactType contact) {
    if (contact == null) {
      return;
    }

    setLimited(
        entity::setContactName,
        safeValue(contact.getName(), NameType::getValue),
        TamanoCampos.TAMANO_500);

    setLimited(
        entity::setContactElectronicMail,
        safeValue(contact.getElectronicMail(), ElectronicMailType::getValue),
        TamanoCampos.TAMANO_500);

    setLimited(
        entity::setContactTelephone,
        safeValue(contact.getTelephone(), TelephoneType::getValue),
        TamanoCampos.TAMANO_500);
  }

  private static void mapPartyIdentifications(
      ContractFolderStatus entity, List<PartyIdentificationType> identifications) {

    // Según tu contexto: lista no nula.
    for (PartyIdentificationType identification : identifications) {
      applyPartyIdentification(entity, identification);
    }
  }

  private static void applyPartyIdentification(
      ContractFolderStatus entity, PartyIdentificationType identification) {

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
      case Constantes.DIR3 -> entity.setDir3(limitedId);
      case Constantes.IDPLATAFORMA -> entity.setIdPlataforma(limitedId);
      case Constantes.IDOCPLAT -> entity.setIdOcPlat(limitedId);
      case Constantes.NIF -> entity.setNif(limitedId);
      case Constantes.OTROS -> {
        entity.setOtros(limitedId);
        entity.setNif(limitedId);
      }
      default -> {
        // Esquema desconocido: intencionalmente ignorado.
      }
    }
  }

  private static void mapDocumentReferences(
      ContractFolderStatus cfs, ContractFolderStatusType type) {
    mapSingleDocumentReference(
        cfs,
        type.getTechnicalDocumentReference(),
        TipoDocumento.PPT,
        DocumentReferenceLink.TECHNICAL);

    mapSingleDocumentReference(
        cfs, type.getLegalDocumentReference(), TipoDocumento.PCAP, DocumentReferenceLink.LEGAL);

    if (!type.getAdditionalDocumentReference().isEmpty()) {
      List<DocumentReference> list =
          MapperDocumentReference.getListDocumentReferenceFromType(
              cfs, TipoDocumento.ADDICIONAL, type.getAdditionalDocumentReference());
      cfs.setAdditionalDocumentReferenceList(list);
    }

    if (!type.getGeneralDocument().isEmpty()) {
      List<DocumentReference> list =
          MapperGeneralDocument.getListGeneralDocumentFromType(
              cfs, null, TipoDocumento.GENERAL, type.getGeneralDocument());
      cfs.setGeneralDocumentReferenceList(list);
    }
  }

  private static void mapSingleDocumentReference(
      ContractFolderStatus entity,
      DocumentReferenceType documentReferenceType,
      TipoDocumento tipoDocumento,
      DocumentReferenceLink link) {

    if (documentReferenceType == null) {
      return;
    }

    DocumentReference doc =
        MapperDocumentReference.getDocumentReferenceFromType(documentReferenceType);
    doc.setTipoDocumento(tipoDocumento);
    link.attach(entity, doc);

    if (link == DocumentReferenceLink.TECHNICAL) {
      entity.setTechnicalDocumentReference(doc);
    } else {
      entity.setLegalDocumentReference(doc);
    }
  }

  private static void mapProcurement(ContractFolderStatus cfs, ContractFolderStatusType type) {
    cfs.setProcurementProject(
        MapperProcurementProject.getProcurementProjectFromType(
            cfs, null, type.getProcurementProject()));

    if (!type.getProcurementProjectLot().isEmpty()) {
      cfs.setProcurementProjectLotList(
          MapperProcurementProjectLot.getListProcurementProjectLotFromType(
              cfs, type.getProcurementProjectLot()));
    }

    if (!type.getTenderResult().isEmpty()) {
      cfs.setTenderResultList(
          MapperTenderResult.getListTenderResultFromType(cfs, type.getTenderResult()));
    }

    if (!type.getValidNoticeInfo().isEmpty()) {
      cfs.setValideNoticeInfoList(
          MapperNoticeInfo.getListNoticeInfoFromType(cfs, null, type.getValidNoticeInfo()));
    }

    if (!type.getContractModification().isEmpty()) {
      cfs.setContractModificationList(
          MapperContractModification.getListContractModificationFromType(
              cfs, type.getContractModification()));
    }
  }

  private static void mapTendering(ContractFolderStatus entity, ContractFolderStatusType type) {
    entity.setTenderingProcess(
        MapperTenderingProcess.getTenderingProcessFromType(
            entity, null, type.getTenderingProcess()));

    entity.setTenderingTerms(mapTenderingTerms(entity, type));
  }

  private static void mapUuid(ContractFolderStatus entity, ContractFolderStatusType type) {
    entity.setUuidList(MapperUuid.getListUuidFromType(entity, type.getUUID()));

    entity.setTenderingTerms(mapTenderingTerms(entity, type));
  }

  private static TenderingTerms mapTenderingTerms(
      ContractFolderStatus entity, ContractFolderStatusType type) {
    TenderingTermsType tenderingTermsType = type.getTenderingTerms();
    if (tenderingTermsType == null) {
      return null;
    }
    return MapperTenderingTerms.getTenderingTermsFromType(entity, null, tenderingTermsType);
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

  /**
   * Encapsula el enlace del {@link DocumentReference} con {@link ContractFolderStatus} para evitar
   * duplicación.
   */
  private enum DocumentReferenceLink {
    TECHNICAL {
      @Override
      void attach(ContractFolderStatus parent, DocumentReference doc) {
        doc.setTechnicalDocumentReference(parent);
      }
    },
    LEGAL {
      @Override
      void attach(ContractFolderStatus parent, DocumentReference doc) {
        doc.setLegalDocumentReference(parent);
      }
    };

    abstract void attach(ContractFolderStatus parent, DocumentReference doc);
  }
}
