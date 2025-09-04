package local.jarios.mappers;

import local.jarios.entity.placsp.ContractExtension;
import local.jarios.entity.placsp.ProcurementProject;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ContractExtensionType;

/**
 * Description: Juan Antonio Author: juan Date: 06/07/2024 Team: Juan Antonio.
 */
@Slf4j
public final class MapperContractExtension {

  private MapperContractExtension() {
  }

  public static ContractExtension getContractExtension(
      ProcurementProject procurementProject,
      ContractExtensionType contractExtensionType) {

    //
    ContractExtension contractExtension = new ContractExtension();

    //
    contractExtension.setProcurementProject(procurementProject);

    //
    contractExtension.setOptionsDescription(
        MapperStringFromList.getStringFromListOptionsDescriptionType(
            contractExtensionType.getOptionsDescription()));

    // ContractExtensionOptions
    if (contractExtensionType.getOptionValidityPeriod() != null) {

      //
      contractExtension.setOptionValidityPeriod(
          MapperPeriod.getPeriod(
              null,
              null,
              contractExtension,
              contractExtensionType.getOptionValidityPeriod()));
    }

    //
    return contractExtension;
  }
}
