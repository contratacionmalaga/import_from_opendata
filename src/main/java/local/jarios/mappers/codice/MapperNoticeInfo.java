package local.jarios.mappers.codice;

import ext.place.codice.common.caclib.AdditionalPublicationDocumentReferenceType;
import ext.place.codice.common.caclib.AdditionalPublicationRequestType;
import ext.place.codice.common.caclib.AdditionalPublicationStatusType;
import ext.place.codice.common.caclib.NoticeInfoType;
import ext.place.codice.common.cbclib.SendTimeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.codice.AdditionalPublicationDocumentReference;
import local.jarios.entity.codice.AdditionalPublicationRequest;
import local.jarios.entity.codice.AdditionalPublicationStatus;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.NoticeInfo;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus;
import local.jarios.helpers.GregorianCalendarHelper;
import local.jarios.helpers.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AttachmentType;
import org.dgpe.codice.common.cbclib.DocumentTypeCodeType;
import org.dgpe.codice.common.cbclib.IssueDateType;
import org.dgpe.codice.common.cbclib.NameType;
import org.dgpe.codice.common.cbclib.NoteType;
import org.oasis.ubl.common.udt.TimeType;
import un.unece.uncefact.data.specification.corecomponenttypeschemamodule._2.IdentifierType;
import un.unece.uncefact.data.specification.corecomponenttypeschemamodule._2.TextType;

/** Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio. */
@Slf4j
public final class MapperNoticeInfo {

  private MapperNoticeInfo() {}

  public static List<NoticeInfo> getListNoticeInfoFromType(
      ContractFolderStatus contractFolderStatus,
      PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus,
      List<NoticeInfoType> listNoticeInfoType) {

    return listNoticeInfoType.stream()
        .map(
            noticeInfoType ->
                getNoticeInfoFromType(
                    contractFolderStatus, preliminaryMarketConsultationStatus, noticeInfoType))
        .toList();
  }

  private static NoticeInfo getNoticeInfoFromType(
      ContractFolderStatus contractFolderStatus,
      PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus,
      NoticeInfoType noticeInfoType) {

    NoticeInfo noticeInfo = new NoticeInfo();
    noticeInfo.setContractFolderStatus(contractFolderStatus);
    noticeInfo.setPreliminaryMarketConsultationStatus(preliminaryMarketConsultationStatus);

    // NoticeTypeCode es obligatorio
    noticeInfo.setNoticeTypeCode(
        StringHelper.limit(noticeInfoType.getNoticeTypeCode().getValue(), TamanoCampos.TAMANO_50));

    noticeInfo.setListAdditionalPublicationStatus(
        getListAdditionalPublicationStatusFromType(
            noticeInfo, noticeInfoType.getAdditionalPublicationStatus()));

    return noticeInfo;
  }

  private static List<AdditionalPublicationStatus> getListAdditionalPublicationStatusFromType(
      NoticeInfo noticeInfo,
      List<AdditionalPublicationStatusType> listAdditionalPublicationStatusType) {

    return listAdditionalPublicationStatusType.stream()
        .map(
            additionalPublicationStatusType ->
                getAdditionalPublicationStatusFromType(noticeInfo, additionalPublicationStatusType))
        .toList();
  }

  private static AdditionalPublicationStatus getAdditionalPublicationStatusFromType(
      NoticeInfo noticeInfo, AdditionalPublicationStatusType additionalPublicationStatusType) {

    AdditionalPublicationStatus additionalPublicationStatus = new AdditionalPublicationStatus();
    additionalPublicationStatus.setNoticeInfo(noticeInfo);

    Optional.ofNullable(additionalPublicationStatusType.getPublicationMediaName())
        .map(NameType::getValue)
        .map(value -> StringHelper.limit(value, TamanoCampos.TAMANO_500))
        .ifPresent(additionalPublicationStatus::setPublicationMediaName);

    additionalPublicationStatus.setAdditionalPublicationDocumentReferenceList(
        getListAdditionalPublicationDocumentReferenceFromType(
            additionalPublicationStatus,
            additionalPublicationStatusType.getAdditionalPublicationDocumentReference()));

    additionalPublicationStatus.setAdditionalPublicationRequestList(
        getListAdditionalPublicationRequestFromType(
            additionalPublicationStatus,
            additionalPublicationStatusType.getAdditionalPublicationRequest()));

    return additionalPublicationStatus;
  }

  private static List<AdditionalPublicationDocumentReference>
      getListAdditionalPublicationDocumentReferenceFromType(
          AdditionalPublicationStatus additionalPublicationStatus,
          List<AdditionalPublicationDocumentReferenceType>
              listAdditionalPublicationDocumentReferenceType) {

    Objects.requireNonNull(
        listAdditionalPublicationDocumentReferenceType,
        "listAdditionalPublicationDocumentReferenceType no puede ser null");

    return listAdditionalPublicationDocumentReferenceType.stream()
        .map(
            additionalPublicationDocumentReferenceType ->
                getAdditionalPublicationDocumentReferenceFromType(
                    additionalPublicationStatus, additionalPublicationDocumentReferenceType))
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
            .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_50))
            .orElse(Constantes.CADENA_VACIA));

    Optional.ofNullable(type.getAttachment())
        .map(AttachmentType::getExternalReference)
        .ifPresent(
            ext -> {
              Optional.ofNullable(ext.getDocumentHash())
                  .map(TextType::getValue)
                  .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_500))
                  .ifPresent(doc::setDocumentHash);

              Optional.ofNullable(ext.getFileName())
                  .map(TextType::getValue)
                  .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_500))
                  .ifPresent(doc::setFilename);

              Optional.ofNullable(ext.getURI())
                  .map(IdentifierType::getValue)
                  .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_500))
                  .ifPresent(doc::setUri);
            });

    return doc;
  }

  private static List<AdditionalPublicationRequest> getListAdditionalPublicationRequestFromType(
      AdditionalPublicationStatus additionalPublicationStatus,
      List<AdditionalPublicationRequestType> listAdditionalPublicationDocumentReferenceType) {

    return listAdditionalPublicationDocumentReferenceType.stream()
        .map(
            additionalPublicationRequestFromType ->
                getAdditionalPublicationDocumentReferenceFromType(
                    additionalPublicationStatus, additionalPublicationRequestFromType))
        .toList();
  }

  private static AdditionalPublicationRequest getAdditionalPublicationDocumentReferenceFromType(
      AdditionalPublicationStatus additionalPublicationStatus,
      AdditionalPublicationRequestType type) {

    AdditionalPublicationRequest req = new AdditionalPublicationRequest();
    req.setAdditionalPublicationStatus(additionalPublicationStatus);
    req.setAgencyId(type.getAgencyID().getValue());

    Optional.ofNullable(type.getNote())
        .map(NoteType::getValue)
        .map(value -> StringHelper.limit(value, TamanoCampos.TAMANO_500))
        .ifPresent(req::setNote);

    req.setSendDateTime(getLocalDateTime(type.getSendDate(), type.getSendTime()));

    return req;
  }

  private static LocalDateTime getLocalDateTime(
      org.dgpe.codice.common.cbclib.DateType date, SendTimeType time) {

    if (date == null) {
      return null;
    }

    LocalDate localDate =
        Optional.ofNullable(date.getValue())
            .map(GregorianCalendarHelper::getDateFromXMLGregorianCalendar)
            .orElse(null);

    if (localDate == null) {
      return null;
    }

    LocalTime localTime =
        Optional.ofNullable(time)
            .map(TimeType::getValue)
            .map(GregorianCalendarHelper::getTimeFromXMLGregorianCalendar)
            .orElse(LocalTime.MIDNIGHT);

    return LocalDateTime.of(localDate, localTime);
  }
}
