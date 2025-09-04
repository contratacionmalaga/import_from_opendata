package local.jarios.mappers;

import local.jarios.common.util.Constantes;
import local.jarios.entity.placsp.ClassificationCategory;
import local.jarios.entity.placsp.ClassificationScheme;
import local.jarios.helpers.ComunHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ClassificationCategoryType;
import org.dgpe.codice.common.cbclib.CodeValueType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio.
 */
@Slf4j
public final class MapperClassificationCategory {

  private MapperClassificationCategory() {
  }

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
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(classificationCategory::setCodeValue);

    //
    return classificationCategory;
  }
}
