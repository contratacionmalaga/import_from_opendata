package local.jarios.mappers;

import local.jarios.entity.placsp.ClassificationScheme;
import local.jarios.entity.placsp.TendererQualificationRequest;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ClassificationSchemeType;
import org.dgpe.codice.common.cbclib.NameType;
import org.dgpe.codice.common.cbclib.NoteType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperClassificationScheme {

    private MapperClassificationScheme() { }

    public static List<ClassificationScheme> getListClassificationScheme (
            TendererQualificationRequest tendererQualificationRequest,
            List<ClassificationSchemeType> listClassificationSchemeType) {

        //
        return listClassificationSchemeType.stream()
                .map(type -> getClassificationScheme(tendererQualificationRequest, type))
                .toList();
    }

    private static ClassificationScheme getClassificationScheme (
            TendererQualificationRequest tendererQualificationRequest,
            ClassificationSchemeType classificationSchemeType) {

        //
        ClassificationScheme classificationScheme = new ClassificationScheme();
        classificationScheme.setTendererQualificationRequest(tendererQualificationRequest);

        Optional.ofNullable(classificationSchemeType.getNote())
                .map(NoteType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_2500))
                .ifPresent(classificationScheme::setNote);

        Optional.ofNullable(classificationSchemeType.getName())
                .map(NameType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_2500))
                .ifPresent(classificationScheme::setNote); // OJO: aquí se sobreescribe el note, ¿es correcto?

        classificationScheme.setDescription(
                MapperStringFromList.getStringFromListDescriptionType(
                        classificationSchemeType.getDescription()));

        classificationScheme.setClassificationCategory(
                MapperClassificationCategory.getListClassificationCategory(
                        classificationScheme,
                        classificationSchemeType.getClassificationCategory()));

        //
        log.debug(classificationScheme.toString());

        //
        return classificationScheme;
    }
}
