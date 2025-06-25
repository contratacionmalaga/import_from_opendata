package local.jarios.mappers;

import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.ProcurementProject;
import local.jarios.entity.placsp.ProcurementProjectLot;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ProcurementProjectType;

import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperProcurementProject {

    private MapperProcurementProject() { }

    public static ProcurementProject getProcurementProjectFromType(
            ContractFolderStatus contractFolderStatus,
            ProcurementProjectLot procurementProjectLot,
            ProcurementProjectType procurementProjectType) {

        //
        ProcurementProject procurementProject = new ProcurementProject();
        procurementProject.setContractFolderStatus(contractFolderStatus);
        procurementProject.setProcurementProjectLot(procurementProjectLot);

        Optional.ofNullable(procurementProjectType).ifPresent(ppt -> {

            procurementProject.setName(
                    StringHelper.eliminarCaracteres(
                            MapperStringFromList.getStringFromListNameType(ppt.getName())));

            procurementProject.setDescription(
                    StringHelper.eliminarCaracteres(
                            MapperStringFromList.getStringFromListDescriptionType(ppt.getDescription())));

            Optional.ofNullable(ppt.getTypeCode()).ifPresent(typeCode ->
                    procurementProject.setTypeCode(
                            ComunHelper.limitarRegistro(
                                    typeCode.getValue(),
                                    Constantes.TAMANO_MAXIMO_CAMPO_5)));

            Optional.ofNullable(ppt.getSubTypeCode()).ifPresent(subTypeCode ->
                    procurementProject.setSubtypeCode(
                            ComunHelper.limitarRegistro(
                                    subTypeCode.getValue(),
                                    Constantes.TAMANO_MAXIMO_CAMPO_5)));

            Optional.ofNullable(ppt.getMixContractIndicator()).ifPresent(mixContractIndicator ->
                    procurementProject.setMixContractIndicator(mixContractIndicator.isValue()));

            Optional.ofNullable(ppt.getBudgetAmount()).ifPresent(budgetAmount ->
                    procurementProject.setBudgetAmount(
                            MapperBudgetAmount.getBudgetAmount(procurementProject, budgetAmount)));

            procurementProject.setRequiredCommodityClassification(
                    MapperCommodityClassification.getListCommodityClassification(
                            procurementProject,
                            ppt.getRequiredCommodityClassification()));

            Optional.ofNullable(ppt.getRealizedLocation()).ifPresent(realizedLocation ->
                    procurementProject.setRealizedLocation(
                            MapperLocation.getLocation(procurementProject, null, null, realizedLocation)));

            Optional.ofNullable(ppt.getPlannedPeriod()).ifPresent(plannedPeriod ->
                    procurementProject.setPlannedPeriod(
                            MapperPeriod.getPeriod(procurementProject, null, null, plannedPeriod)));

            Optional.ofNullable(ppt.getContractExtension()).ifPresent(contractExtension ->
                    procurementProject.setContractExtension(
                            MapperContractExtension.getContractExtension(procurementProject, contractExtension)));

        });

        log.debug(procurementProject.toString());

        return procurementProject;
    }
}
