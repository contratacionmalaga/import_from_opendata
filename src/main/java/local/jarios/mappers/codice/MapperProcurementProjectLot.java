package local.jarios.mappers.codice;

import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.ProcurementProject;
import local.jarios.entity.codice.ProcurementProjectLot;
import local.jarios.helpers.ComunHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ProcurementProjectLotType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio
 */
@Slf4j
public final class MapperProcurementProjectLot {

  private MapperProcurementProjectLot() {

  }

  public static List<ProcurementProjectLot> getListProcurementProjectLotFromType(
      ContractFolderStatus contractFolderStatus,
      List<ProcurementProjectLotType> listProcurementProjectLotType) {

    //
    return Optional.ofNullable(listProcurementProjectLotType)
        .map(list -> list.stream()
            .map(procurementProjectLotType -> getProcurementProjectLotFromType(
                contractFolderStatus,
                procurementProjectLotType)
            )
            .toList())
        .orElseGet(List::of);
  }

  private static ProcurementProjectLot getProcurementProjectLotFromType(
      ContractFolderStatus contractFolderStatus,
      ProcurementProjectLotType procurementProjectLotType) {

    //
    ProcurementProjectLot procurementProjectLot = new ProcurementProjectLot();
    procurementProjectLot.setContractFolderStatus(contractFolderStatus);

    Optional.ofNullable(procurementProjectLotType.getID()).ifPresent(
        id -> procurementProjectLot.setLote(
            ComunHelper.limitarRegistro(
                id.getValue(),
                Constantes.TAMANO_MAXIMO_CAMPO_50)
        )
    );

    Optional.ofNullable(procurementProjectLotType.getProcurementProject()).ifPresent(pp -> {
                                                                                       ProcurementProject procurementProject = MapperProcurementProject.getProcurementProjectFromType(
                                                                                           contractFolderStatus,
                                                                                           pp);
                                                                                       procurementProjectLot.setName(procurementProject.getName());
                                                                                       procurementProjectLot.setTotalAmount(procurementProject.getTotalAmount());
                                                                                       procurementProjectLot.setTaxExclusiveAmount(procurementProject.getTaxExclusiveAmount());
                                                                                       procurementProjectLot.setCountrySubentity(procurementProject.getCountrySubentity());
                                                                                       procurementProjectLot.setCountrySubentityCode(procurementProject.getCountrySubentityCode());
                                                                                       procurementProjectLot.setCountryIdentificationCode(
                                                                                           procurementProject.getCountryIdentificationCode());
                                                                                       procurementProjectLot.setCountryIdentificatonName(
                                                                                           procurementProject.getCountryIdentificatonName());

                                                                                       // ListRequiredCommodityClassification
                                                                                       procurementProjectLot.setRequiredCommodityClassificationList(
                                                                                           MapperCommodityClassification.getListCommodityClassificationFromType(
                                                                                               null,
                                                                                               procurementProjectLot,
                                                                                               pp.getRequiredCommodityClassification()
                                                                                           )
                                                                                       );
                                                                                     }
    );

    return procurementProjectLot;
  }
}
