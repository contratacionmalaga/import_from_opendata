package local.jarios.mappers;

import local.jarios.entity.placsp.ContractExecutionRequirement;
import local.jarios.entity.placsp.TenderingTerms;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ContractExecutionRequirementType;
import org.dgpe.codice.common.cbclib.ExecutionRequirementCodeType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperContractExecutionRequirement {

    private MapperContractExecutionRequirement() { }

    public static List<ContractExecutionRequirement> getListContractExcecutionRequirement (
            TenderingTerms tenderingTerms,
            List<ContractExecutionRequirementType> listContractExecutionRequirementType) {

        //
        return listContractExecutionRequirementType.stream()
                .map(type -> getContractExecutionRequirement(tenderingTerms, type))
                .toList();
    }

    private static ContractExecutionRequirement getContractExecutionRequirement (
            TenderingTerms tenderingTerms,
            ContractExecutionRequirementType contractExecutionRequirementType) {

        //
        ContractExecutionRequirement contractExecutionRequirement = new ContractExecutionRequirement();
        contractExecutionRequirement.setTenderingTerms(tenderingTerms);

        Optional.ofNullable(contractExecutionRequirementType.getExecutionRequirementCode())
                .map(ExecutionRequirementCodeType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(contractExecutionRequirement::setExecutionRequirementCode);

        contractExecutionRequirement.setDescription(
                StringHelper.eliminarCaracteres(
                        MapperStringFromList.getStringFromListDescriptionType(
                                contractExecutionRequirementType.getDescription())));

        contractExecutionRequirement.setName(
                StringHelper.eliminarCaracteres(
                        MapperStringFromList.getStringFromListNameType(
                                contractExecutionRequirementType.getName())));

        return contractExecutionRequirement;
    }
}
