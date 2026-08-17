package local.jarios.mappers.codice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus;
import local.jarios.entity.codice.TenderingProcess;
import local.jarios.helpers.GregorianCalendarHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AuctionTermsType;
import org.dgpe.codice.common.caclib.EconomicOperatorShortListType;
import org.dgpe.codice.common.caclib.PeriodType;
import org.dgpe.codice.common.caclib.TenderingProcessType;
import org.dgpe.codice.common.cbclib.ContractingSystemCodeType;
import org.dgpe.codice.common.cbclib.ExpectedQuantityType;
import org.dgpe.codice.common.cbclib.MaximumQuantityType;
import org.dgpe.codice.common.cbclib.MinimumQuantityType;
import org.dgpe.codice.common.cbclib.PartPresentationCodeType;
import org.dgpe.codice.common.cbclib.ProcedureCodeType;
import org.dgpe.codice.common.cbclib.SubmissionMethodCodeType;
import org.dgpe.codice.common.cbclib.UrgencyCodeType;
import org.oasis.ubl.common.udt.DateType;
import org.oasis.ubl.common.udt.IndicatorType;
import org.oasis.ubl.common.udt.TimeType;

/**
 * Description: Subtipos del modelo Codice. <b>Autor:</b> Juan Antonio <b>Fecha:</b> 06/07/2024
 * <b>Equipo:</b> Juan Antonio
 */
@Slf4j
public final class MapperTenderingProcess {

  private MapperTenderingProcess() {}

  public static TenderingProcess getTenderingProcessFromType(
      ContractFolderStatus contractFolderStatus,
      PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus,
      TenderingProcessType tenderingProcessType) {

    TenderingProcess tenderingProcess = new TenderingProcess();
    tenderingProcess.setContractFolderStatus(contractFolderStatus);
    tenderingProcess.setPreliminaryMarketConsultationStatus(preliminaryMarketConsultationStatus);

    if (tenderingProcessType == null) {
      return tenderingProcess; // devuelves el objeto con lo mínimo seteado
    }

    Optional.ofNullable(tenderingProcessType.getProcedureCode())
        .map(ProcedureCodeType::getValue)
        .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_50))
        .ifPresent(tenderingProcess::setProcedureCode);

    Optional.ofNullable(tenderingProcessType.getContractingSystemCode())
        .map(ContractingSystemCodeType::getValue)
        .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_50))
        .ifPresent(tenderingProcess::setContractingSystemCode);

    Optional.ofNullable(tenderingProcessType.getUrgencyCode())
        .map(UrgencyCodeType::getValue)
        .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_50))
        .ifPresent(tenderingProcess::setUrgencyCode);

    Optional.ofNullable(tenderingProcessType.getSubmissionMethodCode())
        .map(SubmissionMethodCodeType::getValue)
        .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_50))
        .ifPresent(tenderingProcess::setSubmissionMethodCode);

    Optional.ofNullable(tenderingProcessType.getPartPresentationCode())
        .map(PartPresentationCodeType::getValue)
        .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_50))
        .ifPresent(tenderingProcess::setPartPresentationCode);

    Optional.ofNullable(tenderingProcessType.getMaximumLotPresentationQuantity())
        .map(value -> value.getValue().doubleValue())
        .ifPresent(tenderingProcess::setMaximumLotPresentationQuantity);

    Optional.ofNullable(tenderingProcessType.getMaximumTendererAwardedLotsQuantity())
        .map(value -> value.getValue().doubleValue())
        .ifPresent(tenderingProcess::setMaximunTendererAwardedLotQuantity);

    Optional.ofNullable(
            MapperStringFromList.getStringFromListLotsCombinationContractingAuthorityRightsType(
                tenderingProcessType.getLotsCombinationContractingAuthorityRights()))
        .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_500))
        .ifPresent(tenderingProcess::setLotsCombinationContractingAuthorityRights);

    Optional.ofNullable(tenderingProcessType.getAuctionTerms())
        .map(AuctionTermsType::getAuctionConstraintIndicator)
        .map(IndicatorType::isValue)
        .ifPresent(tenderingProcess::setAuctionConstraintIndicator);

    Optional.ofNullable(tenderingProcessType.getOverThresholdIndicator())
        .map(IndicatorType::isValue)
        .ifPresent(tenderingProcess::setOverThresholdIndicator);

    Optional.ofNullable(
            MapperStringFromList.getStringFromListDescriptionType(
                tenderingProcessType.getDescription()))
        .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_500))
        .ifPresent(tenderingProcess::setTenderingProcessDescription);

    mapTenderSubmissionDeadlinePeriod(
        tenderingProcess, tenderingProcessType.getTenderSubmissionDeadlinePeriod());
    mapDocumentAvailabilityPeriod(
        tenderingProcess, tenderingProcessType.getTenderSubmissionDeadlinePeriod());
    mapEconomicOperatorShortList(
        tenderingProcess, tenderingProcessType.getEconomicOperatorShortList());

    return tenderingProcess;
  }

  private static void mapTenderSubmissionDeadlinePeriod(TenderingProcess tp, PeriodType type) {
    LocalDateTime value = getLocalDateTimeFromPeriodType(type);
    if (value != null) {
      tp.setTenderSubmissionDeadlinePeriod(value);
    }
  }

  private static void mapDocumentAvailabilityPeriod(TenderingProcess tp, PeriodType type) {
    LocalDateTime value = getLocalDateTimeFromPeriodType(type);
    if (value != null) {
      tp.setDocumentAvailabilityPeriod(value);
    }
  }

  private static LocalDateTime getLocalDateTimeFromPeriodType(PeriodType periodType) {
    if (periodType == null) {
      return null;
    }

    LocalDate endDate =
        Optional.ofNullable(periodType.getEndDate())
            .map(DateType::getValue)
            .map(GregorianCalendarHelper::getDateFromXMLGregorianCalendar)
            .orElse(null);

    if (endDate == null) {
      return null;
    }

    LocalTime endTime =
        Optional.ofNullable(periodType.getEndTime())
            .map(TimeType::getValue)
            .map(GregorianCalendarHelper::getTimeFromXMLGregorianCalendar)
            .orElse(LocalTime.MIDNIGHT);

    return LocalDateTime.of(endDate, endTime);
  }

  private static void mapEconomicOperatorShortList(
      TenderingProcess tenderingProcess,
      EconomicOperatorShortListType economicOperatorShortListType) {
    if (economicOperatorShortListType == null) {
      return;
    }

    Optional.ofNullable(
            MapperStringFromList.getStringFromListLimitationDescriptionType(
                economicOperatorShortListType.getLimitationDescription()))
        .map(v -> StringHelper.limit(v, TamanoCampos.TAMANO_500))
        .ifPresent(tenderingProcess::setEconomicShortListDescription);

    Optional.ofNullable(economicOperatorShortListType.getExpectedQuantity())
        .map(ExpectedQuantityType::getValue)
        .map(Number::doubleValue)
        .ifPresent(tenderingProcess::setExpectedQuantity);

    Optional.ofNullable(economicOperatorShortListType.getMaximumQuantity())
        .map(MaximumQuantityType::getValue)
        .map(Number::doubleValue)
        .ifPresent(tenderingProcess::setMaximumQuantity);

    Optional.ofNullable(economicOperatorShortListType.getMinimumQuantity())
        .map(MinimumQuantityType::getValue)
        .map(Number::doubleValue)
        .ifPresent(tenderingProcess::setMinimumQuantity);
  }
}
