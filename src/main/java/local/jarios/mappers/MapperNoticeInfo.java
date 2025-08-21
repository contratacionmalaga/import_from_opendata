package local.jarios.mappers;

import ext.place.codice.common.caclib.AdditionalPublicationDocumentReferenceType;
import ext.place.codice.common.caclib.AdditionalPublicationRequestType;
import ext.place.codice.common.caclib.AdditionalPublicationStatusType;
import ext.place.codice.common.caclib.NoticeInfoType;
import local.jarios.common.util.Constantes;
import local.jarios.entity.placsp.AdditionalPublicationDocumentReference;
import local.jarios.entity.placsp.AdditionalPublicationRequest;
import local.jarios.entity.placsp.AdditionalPublicationStatus;
import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.NoticeInfo;
import local.jarios.entity.placsp.PreliminaryMarketConsultationStatus;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.GregorianCalendarHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.cbclib.AgencyIDType;
import org.dgpe.codice.common.cbclib.DocumentTypeCodeType;
import org.dgpe.codice.common.cbclib.IssueDateType;
import org.dgpe.codice.common.cbclib.NameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperNoticeInfo {

  private MapperNoticeInfo() {
  }

  public static List<NoticeInfo> getListNoticeInfoFromType(
      ContractFolderStatus contractFolderStatus,
      PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus,
      List<NoticeInfoType> listNoticeInfoType) {

    List<NoticeInfo> listNoticeInfo = new ArrayList<>();

    for (NoticeInfoType noticeInfoType : listNoticeInfoType) {
      listNoticeInfo.add(getNoticeInfoFromType(
          contractFolderStatus, preliminaryMarketConsultationStatus, noticeInfoType));
    }

    return listNoticeInfo;
  }

  private static NoticeInfo getNoticeInfoFromType(
      ContractFolderStatus contractFolderStatus,
      PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus,
      NoticeInfoType noticeInfoType) {

    NoticeInfo noticeInfo = new NoticeInfo();

    noticeInfo.setContractFolderStatus(contractFolderStatus);
    noticeInfo.setPreliminaryMarketConsultationStatus(preliminaryMarketConsultationStatus);

    // NoticeTypeCode es obligatorio, sin Optional porque se asume que no es null
    noticeInfo
        .setNoticeTypeCode(ComunHelper.limitarRegistro(
            noticeInfoType.getNoticeTypeCode().getValue(),
            Constantes.TAMANO_MAXIMO_CAMPO_50));

    noticeInfo
        .setListAdditionalPublicationStatus(
            getListAdditionalPublicationStatusFromType(
                noticeInfo,
                Optional
                    .ofNullable(noticeInfoType
                        .getAdditionalPublicationStatus())
                    .orElse(Collections.emptyList())));

    return noticeInfo;
  }

  private static List<AdditionalPublicationStatus> getListAdditionalPublicationStatusFromType(
      NoticeInfo noticeInfo,
      List<AdditionalPublicationStatusType> listAdditionalPublicationStatusType) {

    List<AdditionalPublicationStatus> listAdditionalPublicationStatus = new ArrayList<>();

    for (AdditionalPublicationStatusType additionalPublicationStatusType :
        listAdditionalPublicationStatusType) {
      listAdditionalPublicationStatus
          .add(
              getAdditionalPublicationStatusFromType(
                  noticeInfo,
                  additionalPublicationStatusType));
    }

    return listAdditionalPublicationStatus;
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

    additionalPublicationStatus.setAdditionalPublicationRequestList(
        getListAdditionalPublicationRequestFromType(
            additionalPublicationStatus,
            Optional.ofNullable(additionalPublicationStatusType.getAdditionalPublicationRequest())
                .orElse(Collections.emptyList())));

    additionalPublicationStatus.setAdditionalPublicationDocumentReferenceList(
        getListAdditionalPublicationDocumentReferenceFromType(
            additionalPublicationStatus,
            Optional
                .ofNullable(additionalPublicationStatusType
                    .getAdditionalPublicationDocumentReference())
                .orElse(Collections.emptyList())));

    return additionalPublicationStatus;
  }

  private static List<AdditionalPublicationRequest> getListAdditionalPublicationRequestFromType(
      AdditionalPublicationStatus additionalPublicationStatus,
      List<AdditionalPublicationRequestType> listAdditionalPublicationRequestType) {

    List<AdditionalPublicationRequest> listAdditionalPublicationRequest = new ArrayList<>();

    for (AdditionalPublicationRequestType additionalPublicationRequestType :
        listAdditionalPublicationRequestType) {
      listAdditionalPublicationRequest.add(
          getAdditionalPublicationRequestFromType(
              additionalPublicationStatus,
              additionalPublicationRequestType));
    }

    return listAdditionalPublicationRequest;
  }

  private static AdditionalPublicationRequest getAdditionalPublicationRequestFromType(
      AdditionalPublicationStatus additionalPublicationStatus,
      AdditionalPublicationRequestType additionalPublicationRequestType) {

    AdditionalPublicationRequest additionalPublicationRequest = new AdditionalPublicationRequest();

    additionalPublicationRequest.setAdditionalPublicationStatus(additionalPublicationStatus);

    Optional.ofNullable(additionalPublicationRequestType.getAgencyID())
        .map(AgencyIDType::getValue)
        .map(id -> ComunHelper.limitarRegistro(id, Constantes.TAMANO_MAXIMO_CAMPO_500))
        .ifPresent(additionalPublicationRequest::setAgencyId);

    if (additionalPublicationRequestType.getSendDate() != null &&
        additionalPublicationRequestType.getSendTime() != null) {
      additionalPublicationRequest.setSendDateTime(
          LocalDateTime.of(
              GregorianCalendarHelper.getDateFromXMLGregorianCalendar(
                  additionalPublicationRequestType.getSendDate().getValue()),
              GregorianCalendarHelper.getTimeFromXMLGregorianCalendar(
                  additionalPublicationRequestType.getSendTime().getValue())));
    }

    //
    return additionalPublicationRequest;
  }

  private static List<AdditionalPublicationDocumentReference>
  getListAdditionalPublicationDocumentReferenceFromType(
      AdditionalPublicationStatus additionalPublicationStatus,
      List<AdditionalPublicationDocumentReferenceType>
          listAdditionalPublicationDocumentReferenceType) {

    List<AdditionalPublicationDocumentReference> listAdditionalPublicationDocumentReference =
        new ArrayList<>();

    for (AdditionalPublicationDocumentReferenceType additionalPublicationDocumentReferenceType
        : listAdditionalPublicationDocumentReferenceType) {
      listAdditionalPublicationDocumentReference.add(
          getAdditionalPublicationDocumentReferenceFromType(
              additionalPublicationStatus,
              additionalPublicationDocumentReferenceType));
    }

    return listAdditionalPublicationDocumentReference;
  }

  private static AdditionalPublicationDocumentReference
  getAdditionalPublicationDocumentReferenceFromType(
      AdditionalPublicationStatus additionalPublicationStatus,
      AdditionalPublicationDocumentReferenceType additionalPublicationDocumentReferenceType) {

    AdditionalPublicationDocumentReference additionalPublicationDocumentReference =
        new AdditionalPublicationDocumentReference();

    additionalPublicationDocumentReference
        .setAdditionalPublicationStatus(additionalPublicationStatus);

    Optional.ofNullable(additionalPublicationDocumentReferenceType.getIssueDate())
        .map(IssueDateType::getValue)
        .map(GregorianCalendarHelper::getDateFromXMLGregorianCalendar)
        .ifPresent(additionalPublicationDocumentReference::setIssueDate);

    String docTypeCode = Optional
        .ofNullable(additionalPublicationDocumentReferenceType.getDocumentTypeCode())
        .map(DocumentTypeCodeType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .orElse(Constantes.CADENA_VACIA);
    additionalPublicationDocumentReference.setDocumentTypeCode(docTypeCode);

    Optional.ofNullable(additionalPublicationDocumentReferenceType.getAttachment())
        .map(att -> MapperAttachment
            .getAttachmentFromType(
            null,
                additionalPublicationDocumentReference,
                null,
                att))
        .ifPresent(additionalPublicationDocumentReference::setAttachment);

    //
    return additionalPublicationDocumentReference;
  }
}
