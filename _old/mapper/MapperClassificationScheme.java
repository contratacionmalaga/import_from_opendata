package local.jarios.mappers.codice;

import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.ClassificationScheme;
import local.jarios.entity.codice.TendererQualificationRequest;
import local.jarios.helpers.ComunHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ClassificationSchemeType;
import org.dgpe.codice.common.cbclib.IDType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio.
 */
@Slf4j
public final class MapperClassificationScheme {

  private MapperClassificationScheme() {
  }

  public static List<ClassificationScheme> getListClassificationScheme(
      TendererQualificationRequest tendererQualificationRequest,
      List<ClassificationSchemeType> listClassificationSchemeType) {

    //
    return listClassificationSchemeType.stream()
        .map(type ->
                 getClassificationScheme(tendererQualificationRequest, type))
        .toList();
  }

  private static ClassificationScheme getClassificationScheme(
      TendererQualificationRequest tendererQualificationRequest,
      ClassificationSchemeType classificationSchemeType) {

    //
    ClassificationScheme classificationScheme = new ClassificationScheme();
    classificationScheme.setTendererQualificationRequest(tendererQualificationRequest);

    Optional.ofNullable(classificationSchemeType.getID())
        .map(IDType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_500))
        .ifPresent(classificationScheme::setClassificationSchemeId);


    classificationScheme.setClassificationCategory(
        MapperClassificationCategory.getListClassificationCategory(
            classificationScheme,
            classificationSchemeType.getClassificationCategory()));

    //
    return classificationScheme;
  }
}
