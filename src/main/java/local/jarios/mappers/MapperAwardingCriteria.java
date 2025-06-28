package local.jarios.mappers;

import local.jarios.entity.placsp.AwardingCriteria;
import local.jarios.entity.placsp.AwardingTerms;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AwardingCriteriaType;
import org.dgpe.codice.common.cbclib.AwardingCriteriaSubTypeCodeType;
import org.dgpe.codice.common.cbclib.AwardingCriteriaTypeCodeType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperAwardingCriteria {

    private MapperAwardingCriteria() {
    }

    public static List<AwardingCriteria> getListAwardingCriteria(
            AwardingTerms awardingTerms,
            List<AwardingCriteriaType> listAwardingCriteriaType) {

        // Java 16+. Si usas Java 8, reemplaza con .collect(Collectors.toList())
        return listAwardingCriteriaType.stream()
                .map(type -> getAwardingCriteria(awardingTerms, type))
                .toList();
    }


    private static AwardingCriteria getAwardingCriteria(
            AwardingTerms awardingTerms,
            AwardingCriteriaType awardingCriteriaType) {

        //
        AwardingCriteria awardingCriteria = new AwardingCriteria();

        //
        awardingCriteria.setAwardingTerms(awardingTerms);

        // AwardingCriteriaTypeCode
        Optional.ofNullable(awardingCriteriaType.getAwardingCriteriaTypeCode())
                .map(AwardingCriteriaTypeCodeType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(awardingCriteria::setAwardingCriteriaTypeCode);

        // AwardingCriteriaSubTypeCodeType
        Optional.ofNullable(awardingCriteriaType.getAwardingCriteriaSubTypeCode())
                .map(AwardingCriteriaSubTypeCodeType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(awardingCriteria::setAwardingCriteriaSubTypeCode);

        // WeightNumeric
        Optional.ofNullable(awardingCriteriaType.getWeightNumeric())
                .map(n -> n.getValue().doubleValue())
                .ifPresent(awardingCriteria::setWeightNumeric);

        // Description (probablemente no nulo, pero igual se puede proteger si hace falta)
        awardingCriteria.setDescription(
                MapperStringFromList.getStringFromListDescriptionType(
                        awardingCriteriaType.getDescription()));

        // Note
        awardingCriteria.setNote(
                MapperStringFromList.getStringFromListNoteType(
                        awardingCriteriaType.getNote()));

        //
        return awardingCriteria;
    }
}
