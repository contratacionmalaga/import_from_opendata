package local.jarios.mappers.codice;

import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.Contract;
import local.jarios.entity.codice.TenderResult;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.GregorianCalendarHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ContractType;
import org.dgpe.codice.common.cbclib.IDType;
import org.dgpe.codice.common.cbclib.IssueDateType;

import java.util.Optional;

/**
 * Description: Juan Antonio Author: juan Date: 06/07/2024 Team: Juan Antonio.
 */
@Slf4j
public final class MapperContract {

  private MapperContract() {
  }

  public static Contract getContract(
      TenderResult tenderResult,
      ContractType contractType) {

    //
    Contract contract = new Contract();
    contract.setTenderResult(tenderResult);

    Optional.ofNullable(contractType.getID())
        .map(IDType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(contract::setIdContract);

    Optional.ofNullable(contractType.getIssueDate())
        .map(IssueDateType::getValue)
        .map(GregorianCalendarHelper::getDateFromXMLGregorianCalendar)
        .ifPresent(contract::setIssueDate);

    //
    return contract;
  }
}
