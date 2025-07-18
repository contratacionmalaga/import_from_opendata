package local.jarios.mappers;

import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.TenderResult;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.GregorianCalendarHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.TenderResultType;
import org.dgpe.codice.common.cbclib.AbnormallyLowTendersIndicatorType;
import org.dgpe.codice.common.cbclib.ResultCodeType;
import org.dgpe.codice.common.cbclib.SMEAwardedIndicatorType;
import org.dgpe.codice.common.cbclib.StartDateType;

import java.util.List;
import java.util.Optional;

/**
 * Utilidad para mapear objetos del modelo Codice {@link TenderResultType}
 * a entidades persistentes {@link TenderResult}.
 * <p>
 * Esta clase ofrece métodos estáticos para transformar listas y objetos individuales
 * del tipo {@link TenderResultType} en entidades JPA adecuadas para la persistencia.
 * </p>
 * <p>
 * En la conversión se aplican límites de tamaño, limpieza de cadenas y mapeos
 * de sub-objetos relacionados.
 * </p>
 * <p>
 * La clase es final y no instanciable.
 * </p>
 *
 * <p><b>Autor:</b> Juan Antonio</p>
 * <p><b>Fecha:</b> 11/04/2024</p>
 * <p><b>Equipo:</b> Juan Antonio</p>
 */
@Slf4j
public final class MapperTenderResult {

    private MapperTenderResult() { }

    /**
     * Convierte una lista de objetos {@link TenderResultType} en una lista
     * de entidades {@link TenderResult} asociadas a un {@link ContractFolderStatus}.
     *
     * @param contractFolderStatus entidad padre a la que se asocian los resultados de licitación.
     * @param listTenderResultType lista de objetos {@link TenderResultType} a convertir.
     * @return lista de entidades {@link TenderResult} generadas.
     */
    public static List<TenderResult> getListTenderResultFromType(
            ContractFolderStatus contractFolderStatus,
            List<TenderResultType> listTenderResultType) {

        return listTenderResultType.stream()
                .map(tenderResultType -> getTenderResultFromType(contractFolderStatus, tenderResultType))
                .toList();
    }

    /**
     * Convierte un objeto {@link TenderResultType} en una entidad {@link TenderResult},
     * realizando mapeos de sus atributos y sub-objetos relacionados.
     *
     * @param contractFolderStatus entidad padre para el mapeo.
     * @param tenderResultType objeto fuente a convertir.
     * @return entidad {@link TenderResult} resultante.
     */
    private static TenderResult getTenderResultFromType(
            ContractFolderStatus contractFolderStatus, TenderResultType tenderResultType) {

        var tenderResult = new TenderResult();
        tenderResult.setContractFolderStatus(contractFolderStatus);

        Optional.ofNullable(tenderResultType.getResultCode())
                .map(ResultCodeType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(tenderResult::setResultCode);

        Optional.ofNullable(tenderResultType.getReceivedTenderQuantity())
                .map(qty -> qty.getValue().doubleValue())
                .ifPresent(tenderResult::setReceivedTenderQuantity);

        tenderResult.setDescription(
                StringHelper.eliminarCaracteres(
                        MapperStringFromList.getStringFromListDescriptionType(tenderResultType.getDescription())));

        Optional.ofNullable(tenderResultType.getAwardDate())
                .map(date -> GregorianCalendarHelper.getDateFromXMLGregorianCalendar(date.getValue()))
                .ifPresent(tenderResult::setAwardDate);

        Optional.ofNullable(tenderResultType.getLowerTenderAmount())
                .map(amount -> amount.getValue().doubleValue())
                .ifPresent(tenderResult::setLowerTenderAmountQuantity);

        Optional.ofNullable(tenderResultType.getHigherTenderAmount())
                .map(amount -> amount.getValue().doubleValue())
                .ifPresent(tenderResult::setHigherTenderAmountQuantity);

        Optional.ofNullable(tenderResultType.getAbnormallyLowTendersIndicator())
                .map(AbnormallyLowTendersIndicatorType::isValue)
                .ifPresent(tenderResult::setAbnormallyLowTendersIndicator);

        Optional.ofNullable(tenderResultType.getSMEsReceivedTenderQuantity())
                .map(qty -> qty.getValue().doubleValue())
                .ifPresent(tenderResult::setSMEsReceivedTenderQuantity);

        Optional.ofNullable(tenderResultType.getSMEAwardedIndicator())
                .map(SMEAwardedIndicatorType::isValue)
                .ifPresent(tenderResult::setSMEAwardedIndicator);

        Optional.ofNullable(tenderResultType.getEUNationalsReceivedTenderQuantity())
                .map(qty -> qty.getValue().doubleValue())
                .ifPresent(tenderResult::setEUNationalsReceivedTenderQuantity);

        Optional.ofNullable(tenderResultType.getNonEUNationalsReceivedTenderQuantity())
                .map(qty -> qty.getValue().doubleValue())
                .ifPresent(tenderResult::setNonEUNationalsReceivedTenderQuantity);

        Optional.ofNullable(tenderResultType.getStartDate())
                .map(StartDateType::getValue)
                .map(GregorianCalendarHelper::getDateFromXMLGregorianCalendar)
                .ifPresent(tenderResult::setStartDate);

        Optional.ofNullable(tenderResultType.getAwardedOwnerNationalityCode())
                .map(code -> ComunHelper.limitarRegistro(code.getValue(), Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(tenderResult::setAwardedOwnerNationalityCode);

        Optional.ofNullable(tenderResultType.getWinningParty())
                .ifPresent(winningParty -> tenderResult.setWinningParty(
                        MapperWinningParty.getWinningParty(tenderResult, winningParty)));

        Optional.ofNullable(tenderResultType.getContract())
                .ifPresent(contract -> tenderResult.setContract(
                        MapperContract.getContract(tenderResult, contract)));

        Optional.ofNullable(tenderResultType.getAwardedTenderedProject())
                .ifPresent(awardProject -> tenderResult.setAwardedTenderedProject(
                        MapperTenderedProject.getTenderedProjectFromType(tenderResult, awardProject)));

        tenderResult.setListSubcontractTerms(
                MapperSubcontractTerms.getListSubcontractTerms(null, tenderResult, tenderResultType.getSubcontractTerms()));

        return tenderResult;
    }
}
