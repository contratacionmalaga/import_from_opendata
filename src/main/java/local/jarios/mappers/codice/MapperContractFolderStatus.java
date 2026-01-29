package local.jarios.mappers.codice;

import ext.place.codice.common.caclib.ContractFolderStatusType;
import ext.place.codice.common.caclib.LocatedContractingPartyType;
import ext.place.codice.common.cbclib.ContractFolderStatusCodeType;
import local.jarios.common.util.Constantes;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.DocumentReference;
import local.jarios.entity.codice.TenderingTerms;
import local.jarios.enums.TipoDocumento;
import local.jarios.helpers.ComunHelper;
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

import javax.xml.bind.JAXBElement;
import java.util.List;

/**
 * Mapeador de {@link ContractFolderStatusType} a entidad {@link ContractFolderStatus}.
 *
 * <p>Responsabilidad: transformar el modelo JAXB (CÓDICE) a entidades JPA de forma robusta,
 * aplicando límites de longitud según {@link Constantes}.</p>
 */
public final class MapperContractFolderStatus {

  /**
   * Constructor privado para evitar instanciación.
   */
  private MapperContractFolderStatus() {
    // Utility class.
  }

  /**
   * Obtiene la lista de {@link ContractFolderStatus} a partir del contenido de un
   * {@link EntryType}.
   *
   * <p>Precondición (según tu contexto): {@code entryType.getAny()} contiene siempre
   * {@link JAXBElement}.</p>
   *
   * @param entry     entidad {@link Entry} padre (no nula).
   * @param entryType entrada Atom con contenido mixto (no nula).
   * @return lista inmutable de estados de expediente mapeados.
   */
  public static List<ContractFolderStatus> getListContractFolderStatusFromListType(
      Entry entry,
      EntryType entryType) {

    if (entryType == null || entryType.getAny() == null || entryType.getAny().isEmpty()) {
      return List.of();
    }

    return entryType.getAny().stream()
        .map(JAXBElement.class::cast)
        .filter(elem -> elem.getDeclaredType().equals(ContractFolderStatusType.class))
        .map(MapperContractFolderStatus::castToContractFolderStatus)
        .map(type -> mapContractFolderStatus(entry, type))
        .toList();
  }

  @SuppressWarnings("unchecked")
  private static ContractFolderStatusType castToContractFolderStatus(JAXBElement<?> element) {
    // Seguro por el filtro previo del declaredType.
    return ((JAXBElement<ContractFolderStatusType>) element).getValue();
  }

  private static ContractFolderStatus mapContractFolderStatus(Entry entry, ContractFolderStatusType type) {
    ContractFolderStatus entity = new ContractFolderStatus();
    entity.setEntry(entry);

    mapFolderBasics(entity, type);
    mapLocatedContractingParty(entity, type.getLocatedContractingParty());
    mapDocumentReferences(entity, type);
    mapProcurement(entity, type);
    mapTendering(entity, type);

    return entity;
  }

  private static void mapFolderBasics(ContractFolderStatus entity, ContractFolderStatusType type) {
    setLimited(entity::setContractFolderStatusCode,
               safeValue(type.getContractFolderStatusCode(),
                         ContractFolderStatusCodeType::getValue),
               Constantes.TAMANO_MAXIMO_CAMPO_50);

    setLimited(entity::setContractFolderId,
               safeValue(type.getContractFolderID(), ContractFolderIDType::getValue),
               Constantes.TAMANO_MAXIMO_CAMPO_50);
  }

  private static void mapLocatedContractingParty(
      ContractFolderStatus entity,
      LocatedContractingPartyType located) {

    if (located == null) {
      return;
    }

    setLimited(entity::setBuyerProfileUriId,
               safeValue(located.getBuyerProfileURIID(), BuyerProfileURIIDType::getValue),
               Constantes.TAMANO_MAXIMO_CAMPO_500);

    setLimited(entity::setContractingPartyTypeCode,
               safeValue(located.getContractingPartyTypeCode(),
                         ContractingPartyTypeCodeType::getValue),
               Constantes.TAMANO_MAXIMO_CAMPO_500);

    mapParty(entity, located.getParty());
  }

  private static void mapParty(ContractFolderStatus entity, PartyType party) {
    if (party == null) {
      return;
    }

    setLimited(entity::setWebSiteUri,
               safeValue(party.getWebsiteURI(), WebsiteURIType::getValue),
               Constantes.TAMANO_MAXIMO_CAMPO_500);

    // Según tu contexto: listas no nulas (como mínimo vacías).
    entity.setPartyName(
        MapperStringFromList.getStringFromListPartyNameType(party.getPartyName())
    );

    mapAddress(entity, party.getPostalAddress());
    mapContact(entity, party.getContact());
    mapPartyIdentifications(entity, party.getPartyIdentification());
  }

  private static void mapAddress(ContractFolderStatus entity, AddressType address) {
    if (address == null) {
      return;
    }

    entity.setAddressLine(
        MapperStringFromList.getStringFromListAddressLineType(address.getAddressLine())
    );

    setLimited(entity::setCityName,
               safeValue(address.getCityName(), CityNameType::getValue),
               Constantes.TAMANO_MAXIMO_CAMPO_500);

    setLimited(entity::setPostalZone,
               safeValue(address.getPostalZone(), PostalZoneType::getValue),
               Constantes.TAMANO_MAXIMO_CAMPO_500);
  }

  private static void mapContact(ContractFolderStatus entity, ContactType contact) {
    if (contact == null) {
      return;
    }

    setLimited(entity::setContactName,
               safeValue(contact.getName(), NameType::getValue),
               Constantes.TAMANO_MAXIMO_CAMPO_500);

    setLimited(entity::setContactElectronicMail,
               safeValue(contact.getElectronicMail(), ElectronicMailType::getValue),
               Constantes.TAMANO_MAXIMO_CAMPO_500);

    setLimited(entity::setContactTelephone,
               safeValue(contact.getTelephone(), TelephoneType::getValue),
               Constantes.TAMANO_MAXIMO_CAMPO_50);
  }

  private static void mapPartyIdentifications(
      ContractFolderStatus entity,
      List<PartyIdentificationType> identifications) {

    // Según tu contexto: lista no nula.
    for (PartyIdentificationType identification : identifications) {
      applyPartyIdentification(entity, identification);
    }
  }

  private static void applyPartyIdentification(
      ContractFolderStatus entity,
      PartyIdentificationType identification) {

    if (identification == null || identification.getID() == null) {
      return;
    }

    String rawId = identification.getID().getValue();
    String schemeName = identification.getID().getSchemeName();
    if (rawId == null || schemeName == null) {
      return;
    }

    String limitedId = ComunHelper.limitarRegistro(rawId, Constantes.TAMANO_MAXIMO_CAMPO_50);

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

  private static void mapDocumentReferences(ContractFolderStatus entity, ContractFolderStatusType type) {
    mapSingleDocumentReference(
        entity,
        type.getTechnicalDocumentReference(),
        TipoDocumento.PPT,
        DocumentReferenceLink.TECHNICAL);

    mapSingleDocumentReference(
        entity,
        type.getLegalDocumentReference(),
        TipoDocumento.PCAP,
        DocumentReferenceLink.LEGAL);

    if (!type.getAdditionalDocumentReference().isEmpty()) {
      List<DocumentReference> list = MapperDocumentReference.getListDocumentReferenceFromType(
          entity,
          TipoDocumento.ADDICIONAL,
          type.getAdditionalDocumentReference()
      );
      entity.setAdditionalDocumentReferenceList(list);
    }

    if (!type.getGeneralDocument().isEmpty()) {
      List<DocumentReference> list = MapperGeneralDocument.getListGeneralDocumentFromType(
          entity,
          TipoDocumento.GENERAL,
          type.getGeneralDocument()
      );
      entity.setGeneralDocumentReferenceList(list);
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

    DocumentReference doc = MapperDocumentReference.getDocumentReferenceFromType(
        documentReferenceType);
    doc.setTipoDocumento(tipoDocumento);
    link.attach(entity, doc);

    if (link == DocumentReferenceLink.TECHNICAL) {
      entity.setTechnicalDocumentReference(doc);
    } else {
      entity.setLegalDocumentReference(doc);
    }
  }

  private static void mapProcurement(ContractFolderStatus entity, ContractFolderStatusType type) {
    entity.setProcurementProject(
        MapperProcurementProject.getProcurementProjectFromType(
            entity,
            type.getProcurementProject()
        )
    );

    if (!type.getProcurementProjectLot().isEmpty()) {
      entity.setProcurementProjectLotList(
          MapperProcurementProjectLot.getListProcurementProjectLotFromType(
              entity,
              type.getProcurementProjectLot()
          )
      );
    }

    if (!type.getTenderResult().isEmpty()) {
      entity.setTenderResultList(
          MapperTenderResult.getListTenderResultFromType(
              entity,
              type.getTenderResult()
          )
      );
    }

    if (!type.getValidNoticeInfo().isEmpty()) {
      entity.setValideNoticeInfoList(
          MapperNoticeInfo.getListNoticeInfoFromType(
              entity,
              type.getValidNoticeInfo()
          )
      );
    }

    if (!type.getContractModification().isEmpty()) {
      entity.setContractModificationList(
          MapperContractModification.getListContractModificationFromType(
              entity,
              type.getContractModification()
          )
      );
    }
  }

  private static void mapTendering(ContractFolderStatus entity, ContractFolderStatusType type) {
    entity.setTenderingProcess(
        MapperTenderingProcess.getTenderingProcessFromType(
            entity,
            type.getTenderingProcess()
        )
    );

    entity.setTenderingTerms(mapTenderingTerms(entity, type));
  }

  private static TenderingTerms mapTenderingTerms(ContractFolderStatus entity, ContractFolderStatusType type) {
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
      java.util.function.Consumer<String> setter,
      String value,
      int maxLength) {

    if (value == null) {
      return;
    }
    setter.accept(ComunHelper.limitarRegistro(value, maxLength));
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
