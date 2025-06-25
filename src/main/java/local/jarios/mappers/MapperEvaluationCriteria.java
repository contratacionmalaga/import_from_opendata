package local.jarios.mappers;

import local.jarios.entity.placsp.EvaluationCriteria;
import local.jarios.entity.placsp.TendererQualificationRequest;
import local.jarios.enums.TipoSolvencia;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.EvaluationCriteriaType;
import org.dgpe.codice.common.cbclib.EvaluationCriteriaTypeCodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperEvaluationCriteria {

    private MapperEvaluationCriteria() { }

    public static List<EvaluationCriteria> getListEvaluationCriteria (
            TendererQualificationRequest tendererQualificationRequest,
            List<EvaluationCriteriaType> listEvaluationCriteriaType,
            TipoSolvencia tipoSolvencia) {

        //
        var listEvaluationCriteria = new ArrayList<EvaluationCriteria>();

        for (var evaluationCriteriaType : listEvaluationCriteriaType) {
            listEvaluationCriteria.add(
                    getEvaluationCriteria(tendererQualificationRequest, evaluationCriteriaType, tipoSolvencia));
        }

        return listEvaluationCriteria;
    }

    private static EvaluationCriteria getEvaluationCriteria (
            TendererQualificationRequest tendererQualificationRequest,
            EvaluationCriteriaType evaluationCriteriaType,
            TipoSolvencia tipoSolvencia) {

        //
        var evaluationCriteria = new EvaluationCriteria();

        evaluationCriteria.setTendererQualificationRequest(tendererQualificationRequest);
        evaluationCriteria.setTipoSolvencia(tipoSolvencia);

        Optional.ofNullable(evaluationCriteriaType.getEvaluationCriteriaTypeCode())
                .map(EvaluationCriteriaTypeCodeType::getValue)
                .ifPresent(evaluationCriteria::setEvaluationCriteriaTypeCode);

        Optional.ofNullable(evaluationCriteriaType.getThresholdQuantity())
                .map(quantity -> quantity.getValue().doubleValue())
                .ifPresent(evaluationCriteria::setThresholdQuantity);

        evaluationCriteria.setDescription(
                StringHelper.eliminarCaracteres(
                        MapperStringFromList.getStringFromListDescriptionType(evaluationCriteriaType.getDescription())
                )
        );

        log.debug(evaluationCriteria.toString());
        return evaluationCriteria;
    }
}
