package local.jarios.mappers;

import local.jarios.entity.placsp.EconomicOperatorShortList;
import local.jarios.entity.placsp.TenderingProcess;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.EconomicOperatorShortListType;

import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperEconomicOperatorShortList {

  private MapperEconomicOperatorShortList() {
  }

  public static EconomicOperatorShortList getEconomicOperatorShortList(
      TenderingProcess tenderingProcess,
      EconomicOperatorShortListType economicOperatorShortListType) {

    //
    EconomicOperatorShortList economicOperatorShortList = new EconomicOperatorShortList();
    economicOperatorShortList.setTenderingProcess(tenderingProcess);

    economicOperatorShortList.setDescription(
        MapperStringFromList.getStringFromListLimitationDescriptionType(
            economicOperatorShortListType.getLimitationDescription()));

    Optional.ofNullable(economicOperatorShortListType.getMaximumQuantity())
        .map(q -> q.getValue().doubleValue())
        .ifPresent(economicOperatorShortList::setMaximumQuantity);

    Optional.ofNullable(economicOperatorShortListType.getMinimumQuantity())
        .map(q -> q.getValue().doubleValue())
        .ifPresent(economicOperatorShortList::setMinimumQuantity);

    Optional.ofNullable(economicOperatorShortListType.getExpectedQuantity())
        .map(q -> q.getValue().doubleValue())
        .ifPresent(economicOperatorShortList::setExpectedQuantity);

    return economicOperatorShortList;
  }
}
