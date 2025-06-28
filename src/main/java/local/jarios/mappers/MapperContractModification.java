package local.jarios.mappers;

import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.ContractModification;
import local.jarios.entity.placsp.Measure;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.GregorianCalendarHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ContractModificationType;
import org.dgpe.codice.common.cbclib.ContractIDType;
import org.dgpe.codice.common.cbclib.FinalDurationMeasureType;
import org.dgpe.codice.common.cbclib.IDType;
import org.dgpe.codice.common.cbclib.IssueDateType;


import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperContractModification {

    private MapperContractModification() { }

    public static List<ContractModification> getListContractModificatoinFromType(
            ContractFolderStatus contractFolderStatus,
            List<ContractModificationType> listContractModificationType) {

        //
        return listContractModificationType.stream()
                .map(type -> getContractModificatoinFromType(contractFolderStatus, type))
                .toList();
    }

    private static ContractModification getContractModificatoinFromType(
            ContractFolderStatus contractFolderStatus,
            ContractModificationType contractModificationType) {

        //
        ContractModification contractModification = new ContractModification();
        contractModification.setContractFolderStatus(contractFolderStatus);

        Optional.ofNullable(contractModificationType.getContractID())
                .map(ContractIDType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(contractModification::setContractId);

        Optional.ofNullable(contractModificationType.getID())
                .map(IDType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(contractModification::setIdContractModification);

        Optional.ofNullable(contractModificationType.getIssueDate())
                .map(IssueDateType::getValue)
                .map(GregorianCalendarHelper::getDateFromXMLGregorianCalendar)
                .ifPresent(contractModification::setIssueDate);

        contractModification.setNote(
                StringHelper.eliminarCaracteres(
                        MapperStringFromList.getStringFromListNoteType(contractModificationType.getNote())));

        Optional.ofNullable(contractModificationType.getContractModificationLotID())
                .map(Object::toString)
                .ifPresent(contractModification::setContractModificationLotId);

        //
        log.debug(contractModification.toString());

        Optional.ofNullable(contractModificationType.getContractModificationLegalMonetaryTotal())
                .ifPresent(total -> contractModification.setContractModificationLegalMonetaryTotal(
                        MapperLegalMonetaryTotal.getLegalMonetaryTotalFromType(null, contractModification, null, total)));

        Optional.ofNullable(contractModificationType.getFinalLegalMonetaryTotal())
                .ifPresent(total -> contractModification.setContractModificationFinalLegalMonetaryTotal(
                        MapperLegalMonetaryTotal.getLegalMonetaryTotalFromType(null, null, contractModification, total)));

        Optional.ofNullable(contractModificationType.getFinalDurationMeasure())
                .ifPresent(finalDurationMeasure -> contractModification.setFinalDurationMeasure(
                        getMeasureFromFinalDurantionMeasure(contractModificationType.getFinalDurationMeasure())));

        return contractModification;
    }

    private static Measure getMeasureFromFinalDurantionMeasure(FinalDurationMeasureType finalDurationMeasureType) {

        Measure measure = new Measure();

        Optional.ofNullable(finalDurationMeasureType.getValue())
                .ifPresent(measure::setValue);

        Optional.ofNullable(finalDurationMeasureType.getUnitCode())
                .ifPresent(measure::setUnitCode);

        return measure;
    }
}
