package local.jarios.mappers;

import local.jarios.entity.placsp.SubcontractTerms;
import local.jarios.entity.placsp.TenderResult;
import local.jarios.entity.placsp.TenderingTerms;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.SubcontractTermsType;
import org.dgpe.codice.common.cbclib.RateType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Juan Antonio Author: juan Date: 06/07/2024 Team: Juan Antonio
 */
@Slf4j
public final class MapperSubcontractTerms {

  private MapperSubcontractTerms() {
  }

  public static List<SubcontractTerms> getListSubcontractTerms(
      TenderingTerms tenderingTerms,
      TenderResult tenderResult,
      List<SubcontractTermsType> subcontractTermsTypeList) {

    //
    return Optional.ofNullable(subcontractTermsTypeList)
        .map(list -> list.stream()
            .map(subcontractTermsType -> getSubcontractTerms(tenderingTerms, tenderResult,
                                                             subcontractTermsType))
            .toList())
        .orElseGet(List::of);

  }

  private static SubcontractTerms getSubcontractTerms(
      TenderingTerms tenderingTerms,
      TenderResult tenderResult,
      SubcontractTermsType subcontractTermsType) {

    //
    SubcontractTerms subcontractTerms = new SubcontractTerms();

    //
    subcontractTerms.setTenderingTerms(tenderingTerms);
    subcontractTerms.setTenderResult(tenderResult);

    //
    subcontractTerms.setDescription(
        MapperStringFromList.getStringFromListDescriptionType(
            subcontractTermsType.getDescription()));

    //
    Optional.ofNullable(subcontractTermsType.getRate())
        .map(RateType::getValue)
        .map(Number::doubleValue)
        .ifPresent(subcontractTerms::setRate);

    //
    return subcontractTerms;

  }
}
