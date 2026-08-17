package local.jarios.mappers.codice;

import java.util.List;
import java.util.Optional;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.codice.ClassificationCategory;
import local.jarios.entity.codice.ClassificationScheme;
import local.jarios.helpers.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ClassificationCategoryType;
import org.dgpe.codice.common.cbclib.CodeValueType;

/** Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio. */
@Slf4j
public final class MapperClassificationCategory {

  private MapperClassificationCategory() {}

  public static List<ClassificationCategory> getListClassificationCategory(
      ClassificationScheme classificationScheme,
      List<ClassificationCategoryType> listClassificationCategoryType) {

    return listClassificationCategoryType.stream()
        .map(type -> getClassificationCategory(classificationScheme, type))
        .toList(); // Si usas Java 8, reemplaza con .collect(Collectors.toList())
  }

  private static ClassificationCategory getClassificationCategory(
      ClassificationScheme classificationScheme,
      ClassificationCategoryType classificationCategoryType) {

    ClassificationCategory classificationCategory = new ClassificationCategory();
    classificationCategory.setClassificationScheme(classificationScheme);

    Optional.ofNullable(classificationCategoryType.getCodeValue())
        .map(CodeValueType::getValue)
        .map(value -> StringHelper.normalizeAndLimit(value, TamanoCampos.TAMANO_50))
        .ifPresent(classificationCategory::setCodeValue);

    //
    return classificationCategory;
  }
}
