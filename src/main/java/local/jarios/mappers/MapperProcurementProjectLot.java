package local.jarios.mappers;

import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.ProcurementProjectLot;
import local.jarios.helpers.ComunHelper;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ProcurementProjectLotType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperProcurementProjectLot {

    private MapperProcurementProjectLot() { }

    public static List<ProcurementProjectLot> getListProcurementProjectLotFromType(
            ContractFolderStatus contractFolderStatus,
            List<ProcurementProjectLotType> listProcurementProjectLotType) {

        //
        return Optional.ofNullable(listProcurementProjectLotType)
                .map(list -> list.stream()
                        .map(procurementProjectLotType -> getProcurementProjectLotFromType(contractFolderStatus, procurementProjectLotType))
                        .toList())
                .orElseGet(List::of);
    }

    private static ProcurementProjectLot getProcurementProjectLotFromType(
            ContractFolderStatus contractFolderStatus,
            ProcurementProjectLotType procurementProjectLotType) {

        //
        ProcurementProjectLot procurementProjectLot = new ProcurementProjectLot();
        procurementProjectLot.setContractFolderStatus(contractFolderStatus);

        Optional.ofNullable(procurementProjectLotType.getID()).ifPresent(id ->
                procurementProjectLot.setIdLote(
                        ComunHelper.limitarRegistro(id.getValue(), Constantes.TAMANO_MAXIMO_CAMPO_50))
        );

        procurementProjectLot.setProcurementProject(
                MapperProcurementProject.getProcurementProjectFromType(
                        null,
                        procurementProjectLot,
                        procurementProjectLotType.getProcurementProject()));

        Optional.ofNullable(procurementProjectLotType.getTenderingTerms()).ifPresent(tenderingTerms ->
                procurementProjectLot.setTenderingTerms(
                        MapperTenderingTerms.getTenderingTermsFromType(null, procurementProjectLot, tenderingTerms))
        );

        log.debug(procurementProjectLot.toString());

        return procurementProjectLot;
    }
}
