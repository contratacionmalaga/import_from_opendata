package local.jarios.mappers;

import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.Period;
import local.jarios.entity.placsp.TenderingProcess;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.TenderingProcessType;
import org.dgpe.codice.common.cbclib.*;

import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperTenderingProcess {

    private MapperTenderingProcess() { }

    public static TenderingProcess getTenderingProcessFromType(
            ContractFolderStatus contractFolderStatus,
            TenderingProcessType tenderingProcessType) {

        //
        TenderingProcess tenderingProcess = new TenderingProcess();
        tenderingProcess.setContractFolderStatus(contractFolderStatus);

        Optional.ofNullable(tenderingProcessType.getProcedureCode())
                .map(ProcedureCodeType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_5))
                .ifPresent(tenderingProcess::setProcedureCode);

        Optional.ofNullable(tenderingProcessType.getContractingSystemCode())
                .map(ContractingSystemCodeType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_5))
                .ifPresent(tenderingProcess::setContractingSystemCode);

        Optional.ofNullable(tenderingProcessType.getUrgencyCode())
                .map(UrgencyCodeType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_5))
                .ifPresent(tenderingProcess::setUrgencyCode);

        Optional.ofNullable(tenderingProcessType.getSubmissionMethodCode())
                .map(SubmissionMethodCodeType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_5))
                .ifPresent(tenderingProcess::setSubmissionMethodCode);

        Optional.ofNullable(tenderingProcessType.getPartPresentationCode())
                .map(PartPresentationCodeType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_5))
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
                        Constantes.TAMANO_MAXIMO_CAMPO_450));

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
