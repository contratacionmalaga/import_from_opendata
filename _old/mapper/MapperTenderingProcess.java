package local.jarios.mappers.codice;

import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.Period;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus;
import local.jarios.entity.codice.TenderingProcess;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.TenderingProcessType;
import org.dgpe.codice.common.cbclib.ContractingSystemCodeType;
import org.dgpe.codice.common.cbclib.OverThresholdIndicatorType;
import org.dgpe.codice.common.cbclib.PartPresentationCodeType;
import org.dgpe.codice.common.cbclib.ProcedureCodeType;
import org.dgpe.codice.common.cbclib.SubmissionMethodCodeType;
import org.dgpe.codice.common.cbclib.UrgencyCodeType;

import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice.
 * <b>Autor:</b> Juan Antonio
 * <b>Fecha:</b> 06/07/2024
 * <b>Equipo:</b> Juan Antonio
 */
@Slf4j
public final class MapperTenderingProcess {

  private MapperTenderingProcess() {
  }

  public static TenderingProcess getTenderingProcessFromType(
      ContractFolderStatus contractFolderStatus,
      PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus,
      TenderingProcessType tenderingProcessType) {

    //
    TenderingProcess tenderingProcess = new TenderingProcess();
    tenderingProcess.setContractFolderStatus(contractFolderStatus);
    tenderingProcess.setPreliminaryMarketConsultationStatus(preliminaryMarketConsultationStatus);

    Optional.ofNullable(tenderingProcessType.getProcedureCode())
        .map(ProcedureCodeType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(tenderingProcess::setProcedureCode);

    Optional.ofNullable(tenderingProcessType.getContractingSystemCode())
        .map(ContractingSystemCodeType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(tenderingProcess::setContractingSystemCode);

    Optional.ofNullable(tenderingProcessType.getUrgencyCode())
        .map(UrgencyCodeType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(tenderingProcess::setUrgencyCode);

    Optional.ofNullable(tenderingProcessType.getSubmissionMethodCode())
        .map(SubmissionMethodCodeType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(tenderingProcess::setSubmissionMethodCode);

    Optional.ofNullable(tenderingProcessType.getPartPresentationCode())
        .map(PartPresentationCodeType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(tenderingProcess::setPartPresentationCode);

    Optional.ofNullable(tenderingProcessType.getMaximumLotPresentationQuantity())
        .map(value -> value.getValue().doubleValue())
        .ifPresent(tenderingProcess::setMaximumLotPresentationQuantity);

    Optional.ofNullable(tenderingProcessType.getMaximumTendererAwardedLotsQuantity())
        .map(value -> value.getValue().doubleValue())
        .ifPresent(tenderingProcess::setMaximunTendererAwardedLotQuantity);

    tenderingProcess.setLotsCombinationContractingAuthorityRights(
        ComunHelper.limitarRegistro(
            MapperStringFromList.getStringFromListLotsCombinationContractingAuthorityRightsType(
                tenderingProcessType.getLotsCombinationContractingAuthorityRights()),
            Constantes.TAMANO_MAXIMO_CAMPO_500));

    Optional.ofNullable(tenderingProcessType.getOverThresholdIndicator())
        .map(OverThresholdIndicatorType::isValue)
        .ifPresent(tenderingProcess::setOverThresholdIndicator);

    tenderingProcess.setDescription(
        StringHelper.eliminarCaracteres(
            MapperStringFromList.getStringFromListDescriptionType(
                tenderingProcessType.getDescription())));

    Optional.ofNullable(tenderingProcessType.getAuctionTerms())
        .ifPresent(auctionTerms ->
                       tenderingProcess.setAuctionTerms(
                           MapperAuctionTerms.getAuctionTerms(tenderingProcess, auctionTerms)));

    Optional.ofNullable(tenderingProcessType.getEconomicOperatorShortList())
        .ifPresent(economicOperatorShortList ->
                       tenderingProcess.setEconomicOperatorShortList(
                           MapperEconomicOperatorShortList.getEconomicOperatorShortList(
                               tenderingProcess, economicOperatorShortList)));

    Optional.ofNullable(tenderingProcessType.getDocumentAvailabilityPeriod())
        .ifPresent(period -> {
          Period miPeriod = MapperPeriod.getPeriod(
              null,
              tenderingProcess,
              null,
              period);
          tenderingProcess.setDocumentAvailabilityPeriod(miPeriod.getEndDateTime());
        });

    Optional.ofNullable(tenderingProcessType.getTenderSubmissionDeadlinePeriod())
        .ifPresent(period -> {
          Period miPeriod = MapperPeriod.getPeriod(
              null,
              tenderingProcess,
              null,
              period);
          tenderingProcess.setTenderSubmissionDeadlinePeriod(miPeriod.getEndDateTime());
        });

    tenderingProcess.setListProcessJustification(
        MapperProcessJustification.getListProcessJustificationFromType(
            tenderingProcess,
            tenderingProcessType.getProcessJustification()));

    return tenderingProcess;
  }
}
