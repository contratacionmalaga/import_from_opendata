package local.jarios.codice;

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
public final class SubcontractTermsMapper {

  private SubcontractTermsMapper() {
  }

  public static List<SubcontractTerms> getListSubcontractTerms(
      List<SubcontractTermsType> subcontractTermsTypeList) {

    //
    return Optional.ofNullable(subcontractTermsTypeList)
        .map(list -> list.stream()
            .map(SubcontractTermsMapper::getSubcontractTerms)
            .toList())
        .orElseGet(List::of);

  }

  private static SubcontractTerms getSubcontractTerms(
      SubcontractTermsType subcontractTermsType) {

    //
    SubcontractTerms subcontractTerms = new SubcontractTerms();

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
