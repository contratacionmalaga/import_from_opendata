package local.jarios.mappers.codice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.ProcurementProject;
import local.jarios.entity.codice.ProcurementProjectLot;
import local.jarios.entity.codice.TenderingTerms;
import local.jarios.helpers.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ProcurementProjectLotType;

/** Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio */
@Slf4j
public final class MapperProcurementProjectLot {

  private MapperProcurementProjectLot() {}

  public static List<ProcurementProjectLot> getListProcurementProjectLotFromType(
      ContractFolderStatus contractFolderStatus,
      List<ProcurementProjectLotType> listProcurementProjectLotType) {

    //
    return Optional.ofNullable(listProcurementProjectLotType)
        .map(
            list ->
                list.stream()
                    .map(
                        procurementProjectLotType ->
                            getProcurementProjectLotFromType(
                                contractFolderStatus, procurementProjectLotType))
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new)))
        .orElseGet(ArrayList::new);
  }

  private static ProcurementProjectLot getProcurementProjectLotFromType(
      ContractFolderStatus contractFolderStatus,
      ProcurementProjectLotType procurementProjectLotType) {

    ProcurementProjectLot procurementProjectLot = new ProcurementProjectLot();
    procurementProjectLot.setContractFolderStatus(contractFolderStatus);

    if (procurementProjectLotType == null) {
      return procurementProjectLot;
    }

    Optional.ofNullable(procurementProjectLotType.getID())
        .ifPresent(
            id ->
                procurementProjectLot.setLote(
                    StringHelper.limit(id.getValue(), TamanoCampos.TAMANO_50)));

    Optional.ofNullable(procurementProjectLotType.getProcurementProject())
        .ifPresent(
            pp -> {
              ProcurementProject procurementProject =
                  MapperProcurementProject.getProcurementProjectFromType(
                      contractFolderStatus, null, pp);

              // 👇 Aquí usas el método
              applyProcurementProjectDataToLot(procurementProjectLot, procurementProject);

              // ListRequiredCommodityClassification
              procurementProjectLot.setRequiredCommodityClassificationList(
                  MapperCommodityClassification.getListCommodityClassificationFromType(
                      null, procurementProjectLot, pp.getRequiredCommodityClassification()));
            });

    Optional.ofNullable(procurementProjectLotType.getTenderingTerms())
        .ifPresent(
            tt -> {
              TenderingTerms tenderingTerms =
                  MapperTenderingTerms.getTenderingTermsFromType(null, procurementProjectLot, tt);

              procurementProjectLot.setTenderingTerms(tenderingTerms);
            });

    return procurementProjectLot;
  }

  private static void applyProcurementProjectDataToLot(
      ProcurementProjectLot procurementProjectLot, ProcurementProject procurementProject) {
    procurementProjectLot.setName(procurementProject.getName());
    procurementProjectLot.setTotalAmount(procurementProject.getTotalAmount());
    procurementProjectLot.setTaxExclusiveAmount(procurementProject.getTaxExclusiveAmount());
    procurementProjectLot.setCountrySubentity(procurementProject.getCountrySubentity());
    procurementProjectLot.setCountrySubentityCode(procurementProject.getCountrySubentityCode());
    procurementProjectLot.setCountryIdentificationCode(
        procurementProject.getCountryIdentificationCode());
    procurementProjectLot.setCountryIdentificatonName(
        procurementProject.getCountryIdentificationName());
  }
}
