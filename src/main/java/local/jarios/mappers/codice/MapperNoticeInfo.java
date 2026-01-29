package local.jarios.mappers.codice;

import ext.place.codice.common.caclib.AdditionalPublicationDocumentReferenceType;
import ext.place.codice.common.caclib.AdditionalPublicationStatusType;
import ext.place.codice.common.caclib.NoticeInfoType;
import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.AdditionalPublicationDocumentReference;
import local.jarios.entity.codice.AdditionalPublicationStatus;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.NoticeInfo;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.GregorianCalendarHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AttachmentType;
import org.dgpe.codice.common.cbclib.DocumentTypeCodeType;
import org.dgpe.codice.common.cbclib.IssueDateType;
import org.dgpe.codice.common.cbclib.NameType;
import un.unece.uncefact.data.specification.corecomponenttypeschemamodule._2.IdentifierType;
import un.unece.uncefact.data.specification.corecomponenttypeschemamodule._2.TextType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio.
 */
@Slf4j
public final class MapperNoticeInfo {

  private MapperNoticeInfo() {
  }

  public static List<NoticeInfo> getListNoticeInfoFromType(
      ContractFolderStatus contractFolderStatus,
      List<NoticeInfoType> listNoticeInfoType) {

    Objects.requireNonNull(contractFolderStatus, "contractFolderStatus no puede ser null");
    Objects.requireNonNull(listNoticeInfoType, "listNoticeInfoType no puede ser null");

    return listNoticeInfoType.stream()
        .map(noticeInfoType -> getNoticeInfoFromType(contractFolderStatus, noticeInfoType))
        .toList();
  }

  private static NoticeInfo getNoticeInfoFromType(
      ContractFolderStatus contractFolderStatus,
      NoticeInfoType noticeInfoType) {

    NoticeInfo noticeInfo = new NoticeInfo();
    noticeInfo.setContractFolderStatus(contractFolderStatus);

    // NoticeTypeCode es obligatorio
    noticeInfo.setNoticeTypeCode(
        ComunHelper.limitarRegistro(
            noticeInfoType.getNoticeTypeCode().getValue(),
            Constantes.TAMANO_MAXIMO_CAMPO_50
        )
    );

    noticeInfo.setListAdditionalPublicationStatus(
        getListAdditionalPublicationStatusFromType(
            noticeInfo,
            Optional.ofNullable(noticeInfoType.getAdditionalPublicationStatus())
                .orElse(Collections.emptyList())
        )
    );

    return noticeInfo;
  }

  private static List<AdditionalPublicationStatus> getListAdditionalPublicationStatusFromType(
      NoticeInfo noticeInfo,
      List<AdditionalPublicationStatusType> listAdditionalPublicationStatusType) {

    Objects.requireNonNull(
        listAdditionalPublicationStatusType,
        "listAdditionalPublicationStatusType no puede ser null"
    );

    return listAdditionalPublicationStatusType.stream()
        .map(additionalPublicationStatusType ->
                 getAdditionalPublicationStatusFromType(noticeInfo,
                                                        additionalPublicationStatusType))
        .toList();
  }

  private static AdditionalPublicationStatus getAdditionalPublicationStatusFromType(
      NoticeInfo noticeInfo,
      AdditionalPublicationStatusType additionalPublicationStatusType) {

    AdditionalPublicationStatus additionalPublicationStatus = new AdditionalPublicationStatus();
    additionalPublicationStatus.setNoticeInfo(noticeInfo);

    Optional.ofNullable(additionalPublicationStatusType.getPublicationMediaName())
        .map(NameType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_500))
        .ifPresent(additionalPublicationStatus::setPublicationMediaName);

    additionalPublicationStatus.setAdditionalPublicationDocumentReferenceList(
        getListAdditionalPublicationDocumentReferenceFromType(
            additionalPublicationStatus,
            Optional.ofNullable(
                    additionalPublicationStatusType.getAdditionalPublicationDocumentReference()
                )
                .orElse(Collections.emptyList())
        )
    );

    return additionalPublicationStatus;
  }

  private static List<AdditionalPublicationDocumentReference>
  getListAdditionalPublicationDocumentReferenceFromType(
      AdditionalPublicationStatus additionalPublicationStatus,
      List<AdditionalPublicationDocumentReferenceType>
          listAdditionalPublicationDocumentReferenceType) {

    Objects.requireNonNull(
        listAdditionalPublicationDocumentReferenceType,
        "listAdditionalPublicationDocumentReferenceType no puede ser null"
    );

    return listAdditionalPublicationDocumentReferenceType.stream()
        .map(additionalPublicationDocumentReferenceType ->
                 getAdditionalPublicationDocumentReferenceFromType(
                     additionalPublicationStatus,
                     additionalPublicationDocumentReferenceType)
        )
        .toList();
  }

  private static AdditionalPublicationDocumentReference
  getAdditionalPublicationDocumentReferenceFromType(
      AdditionalPublicationStatus additionalPublicationStatus,
      AdditionalPublicationDocumentReferenceType type) {

    AdditionalPublicationDocumentReference doc = new AdditionalPublicationDocumentReference();
    doc.setAdditionalPublicationStatus(additionalPublicationStatus);

    Optional.ofNullable(type.getIssueDate())
        .map(IssueDateType::getValue)
        .map(GregorianCalendarHelper::getDateFromXMLGregorianCalendar)
        .ifPresent(doc::setIssueDate);

    doc.setDocumentTypeCode(
        Optional.ofNullable(type.getDocumentTypeCode())
            .map(DocumentTypeCodeType::getValue)
            .map(v -> ComunHelper.limitarRegistro(v, Constantes.TAMANO_MAXIMO_CAMPO_50))
            .orElse(Constantes.CADENA_VACIA)
    );

    Optional.ofNullable(type.getAttachment())
        .map(AttachmentType::getExternalReference)
        .ifPresent(ext -> {
          Optional.ofNullable(ext.getDocumentHash())
              .map(TextType::getValue)
              .map(v -> ComunHelper.limitarRegistro(v, Constantes.TAMANO_MAXIMO_CAMPO_500))
              .ifPresent(doc::setDocumentHash);

          Optional.ofNullable(ext.getFileName())
              .map(TextType::getValue)
              .map(v -> ComunHelper.limitarRegistro(v, Constantes.TAMANO_MAXIMO_CAMPO_500))
              .ifPresent(doc::setFilename);

          Optional.ofNullable(ext.getURI())
              .map(IdentifierType::getValue)
              .map(v -> ComunHelper.limitarRegistro(v, Constantes.TAMANO_MAXIMO_CAMPO_500))
              .ifPresent(doc::setUri);
        });

    return doc;
  }
}
