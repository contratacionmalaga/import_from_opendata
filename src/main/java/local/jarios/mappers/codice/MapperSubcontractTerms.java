package local.jarios.mappers.codice;

import java.util.Collections;
import java.util.List;
import local.jarios.codice.SubcontractTerms;
import local.jarios.common.util.TamanoCampos;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.SubcontractTermsType;

/** Description: Mapper de SubcontractTerms. Author: Juan Date: 11/04/2024 Team: Juan Antonio */
@Slf4j
public final class MapperSubcontractTerms {

  private MapperSubcontractTerms() {}

  public static List<SubcontractTerms> getListSubcontactTerms(List<SubcontractTermsType> listType) {
    if (listType == null || listType.isEmpty()) {
      return Collections.emptyList();
    }

    return listType.stream().map(MapperSubcontractTerms::getSubcontractTermsFromType).toList();
  }

  private static SubcontractTerms getSubcontractTermsFromType(SubcontractTermsType type) {
    if (type == null) {
      return null;
    }

    SubcontractTerms subcontractTerms = new SubcontractTerms();

    setLimited(
        subcontractTerms::setDescription,
        MapperStringFromList.getStringFromListDescriptionType(type.getDescription()),
        TamanoCampos.TAMANO_2500);

    subcontractTerms.setRate(
        type.getRate().getValue() == null ? null : type.getRate().getValue().doubleValue());

    return subcontractTerms;
  }

  private static <T> String safeValue(T obj, java.util.function.Function<T, String> mapper) {
    if (obj == null) {
      return null;
    }
    return mapper.apply(obj);
  }

  private static void setLimited(
      java.util.function.Consumer<String> setter, String value, int maxLength) {

    if (value == null) {
      return;
    }

    setter.accept(StringHelper.limit(value, maxLength));
  }
}
