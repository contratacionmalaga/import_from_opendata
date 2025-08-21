package local.jarios.mappers;

import ext.place.codice.common.caclib.PreliminaryMarketConsultationStatusType;
import ext.place.codice.common.cbclib.PreliminaryMarketConsultationStatusCodeType;
import local.jarios.common.util.Constantes;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.placsp.PreliminaryMarketConsultationStatus;
import local.jarios.helpers.ComunHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.cbclib.ConditionTypeCodeType;
import org.dgpe.codice.common.cbclib.ConditionsTextType;
import org.dgpe.codice.common.cbclib.ConsultationNameType;
import org.dgpe.codice.common.cbclib.LimitDateType;
import org.dgpe.codice.common.cbclib.PartySelectionReasonTextType;
import org.dgpe.codice.common.cbclib.PlannedDateType;
import org.dgpe.codice.common.cbclib.PreliminaryMarketConsultationIDType;
import org.dgpe.codice.common.cbclib.TextType;
import org.w3._2005.atom.EntryType;

import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperPreliminaryMarketConsultationStatus {

  private MapperPreliminaryMarketConsultationStatus() {
  }

  public static List<PreliminaryMarketConsultationStatus> getListPreliminaryMarketConsultationStatusFromListType(
      Entry entry, EntryType entryType) {

    return entryType.getAny().stream()
        .filter(JAXBElement.class::isInstance)
        .map(JAXBElement.class::cast)
        .filter(elem -> elem.getDeclaredType().equals(PreliminaryMarketConsultationStatusType.class))
        .map(elem -> {
          @SuppressWarnings("unchecked")
          JAXBElement<PreliminaryMarketConsultationStatusType> typedElem = (JAXBElement<PreliminaryMarketConsultationStatusType>) elem;
          return getContractFolderStatusFromType(entry, typedElem);
        })
        .toList();
  }

  private static PreliminaryMarketConsultationStatus getContractFolderStatusFromType(
      Entry entry, JAXBElement<PreliminaryMarketConsultationStatusType> jaxbElement) {

    //
    PreliminaryMarketConsultationStatusType preliminaryMarketConsultationStatusType = jaxbElement.getValue();
    PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus = new PreliminaryMarketConsultationStatus();

    preliminaryMarketConsultationStatus.setEntry(entry);

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getPreliminaryMarketConsultationStatusCode())
        .map(PreliminaryMarketConsultationStatusCodeType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(preliminaryMarketConsultationStatus::setPreliminaryMarketConsultationStatusCode);

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getPreliminaryMarketConsultationID())
        .map(PreliminaryMarketConsultationIDType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(preliminaryMarketConsultationStatus::setPreliminaryMarketConsultationID);

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getConsultationName())
        .map(ConsultationNameType::getValue)
        .ifPresent(preliminaryMarketConsultationStatus::setConsultationName);

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getConditionTypeCode())
        .map(ConditionTypeCodeType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(preliminaryMarketConsultationStatus::setConditionTypeCode);

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getConditionsText())
        .map(ConditionsTextType::getValue)
        .ifPresent(preliminaryMarketConsultationStatus::setConditionsText);

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getPartySelectionReasonText())
        .map(PartySelectionReasonTextType::getValue)
        .ifPresent(preliminaryMarketConsultationStatus::setPartySelectionReasonText);

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getConditionTypeReasonText())
        .map(TextType::getValue)
        .ifPresent(preliminaryMarketConsultationStatus::setConditionTypeReasonText);

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getPlannedDate())
        .map(PlannedDateType::getValue)
        .map(xmlDate -> xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDate())
        .ifPresent(preliminaryMarketConsultationStatus::setPlannedDate);

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getLimitDate())
        .map(LimitDateType::getValue)
        .map(xmlDate -> xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDate())
        .ifPresent(preliminaryMarketConsultationStatus::setLimitDate);

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getProcurementProject())
        .ifPresent(pp -> preliminaryMarketConsultationStatus.setProcurementProject(
            MapperProcurementProject.getProcurementProjectFromType(
                null,
                preliminaryMarketConsultationStatus,
                null,
                pp)));

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getTenderingProcess())
        .ifPresent(tp -> preliminaryMarketConsultationStatus.setTenderingProcess(
            MapperTenderingProcess.getTenderingProcessFromType(
                null,
                preliminaryMarketConsultationStatus,
                tp)));

    Optional.ofNullable(preliminaryMarketConsultationStatusType.getAttachment())
        .ifPresent(attachment -> preliminaryMarketConsultationStatus.setAttachment(
            MapperAttachment.getAttachmentFromType(
                null,
                null,
                preliminaryMarketConsultationStatus, attachment)));

    preliminaryMarketConsultationStatus.setLocatedContractingParty(
        MapperLocatedContractingParty.getLocatedContractingPartyFromType(
            null,
            preliminaryMarketConsultationStatus,
            preliminaryMarketConsultationStatusType.getLocatedContractingParty()));

    preliminaryMarketConsultationStatus.setListGeneralDocument(
        MapperGeneralDocument.getListGeneralDocumentFromType(
            null,
            preliminaryMarketConsultationStatus,
            preliminaryMarketConsultationStatusType.getGeneralDocument()));

    preliminaryMarketConsultationStatus.setListNoticeInfo(
        MapperNoticeInfo.getListNoticeInfoFromType(
            null,
            preliminaryMarketConsultationStatus,
            preliminaryMarketConsultationStatusType.getValidNoticeInfo()));

    return preliminaryMarketConsultationStatus;

  }
}
