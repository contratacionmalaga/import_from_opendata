package local.jarios.mappers.codice;

import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.TenderingProcess;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import org.dgpe.codice.common.caclib.AuctionTermsType;
import org.dgpe.codice.common.caclib.TenderingProcessType;
import org.dgpe.codice.common.cbclib.ContractingSystemCodeType;
import org.dgpe.codice.common.cbclib.PartPresentationCodeType;
import org.dgpe.codice.common.cbclib.ProcedureCodeType;
import org.dgpe.codice.common.cbclib.SubmissionMethodCodeType;
import org.dgpe.codice.common.cbclib.UrgencyCodeType;
import org.oasis.ubl.common.udt.IndicatorType;

import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice.
 * <b>Autor:</b> Juan Antonio
 * <b>Fecha:</b> 06/07/2024
 * <b>Equipo:</b> Juan Antonio
 */
public final class MapperTenderingProcess {

  private MapperTenderingProcess() {
  }

  public static TenderingProcess getTenderingProcessFromType(
      ContractFolderStatus contractFolderStatus,
      TenderingProcessType tenderingProcessType) {

    TenderingProcess tenderingProcess = new TenderingProcess();
    tenderingProcess.setContractFolderStatus(contractFolderStatus);

    if (tenderingProcessType == null) {
      return tenderingProcess; // devuelves el objeto con lo mínimo seteado
    }

    Optional.ofNullable(tenderingProcessType.getProcedureCode())
        .map(ProcedureCodeType::getValue)
        .map(v -> limit(v, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(tenderingProcess::setProcedureCode);

    Optional.ofNullable(tenderingProcessType.getContractingSystemCode())
        .map(ContractingSystemCodeType::getValue)
        .map(v -> limit(v, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(tenderingProcess::setContractingSystemCode);

    Optional.ofNullable(tenderingProcessType.getUrgencyCode())
        .map(UrgencyCodeType::getValue)
        .map(v -> limit(v, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(tenderingProcess::setUrgencyCode);

    Optional.ofNullable(tenderingProcessType.getSubmissionMethodCode())
        .map(SubmissionMethodCodeType::getValue)
        .map(v -> limit(v, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(tenderingProcess::setSubmissionMethodCode);

    Optional.ofNullable(tenderingProcessType.getPartPresentationCode())
        .map(PartPresentationCodeType::getValue)
        .map(v -> limit(v, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(tenderingProcess::setPartPresentationCode);

    Optional.ofNullable(tenderingProcessType.getMaximumLotPresentationQuantity())
        .map(value -> value.getValue().doubleValue())
        .ifPresent(tenderingProcess::setMaximumLotPresentationQuantity);

    Optional.ofNullable(tenderingProcessType.getMaximumTendererAwardedLotsQuantity())
        .map(value -> value.getValue().doubleValue())
        .ifPresent(tenderingProcess::setMaximunTendererAwardedLotQuantity);

    Optional.ofNullable(
            MapperStringFromList.getStringFromListLotsCombinationContractingAuthorityRightsType(
                tenderingProcessType.getLotsCombinationContractingAuthorityRights()
            )
        )
        .map(v -> limit(v, Constantes.TAMANO_MAXIMO_CAMPO_500))
        .ifPresent(tenderingProcess::setLotsCombinationContractingAuthorityRights);


    Optional.ofNullable(tenderingProcessType.getAuctionTerms())
        .map(AuctionTermsType::getAuctionConstraintIndicator)
        .map(IndicatorType::isValue)
        .ifPresent(tenderingProcess::setAuctionConstraintIndicator);

    return tenderingProcess;
  }

  private static String limit(String value, int max) {
    if (value == null) return null;
    String v = value.trim();
    if (v.isBlank()) return null;
    return ComunHelper.limitarRegistro(v, max);
  }
}
