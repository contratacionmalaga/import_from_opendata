package local.jarios.mappers;

import local.jarios.entity.placsp.EvaluationCriteria;
import local.jarios.entity.placsp.TendererQualificationRequest;
import local.jarios.entity.placsp.TenderingTerms;
import local.jarios.enums.TipoSolvencia;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.TendererQualificationRequestType;

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
public final class MapperTendererQualificationRequest {

  private MapperTendererQualificationRequest() {
  }

  public static TendererQualificationRequest getTendererQualificationRequest(
      TenderingTerms tenderingTerms,
      TendererQualificationRequestType tendererQualificationRequestType) {

    //
    TendererQualificationRequest tendererQualificationRequest = new TendererQualificationRequest();
    tendererQualificationRequest.setTenderingTerms(tenderingTerms);

    // Título habilitante: Descripción textual de los requisitos específicos del operador económico para poder
    // participar en la licitación.
    tendererQualificationRequest.setPersonalSituation(
        MapperStringFromList.getStringFromListPersonalSituationType(
            tendererQualificationRequestType.getPersonalSituation()));

    // Solvencia requerida: Descripción textual de la información y trámites necesarios para evaluar si se cumplen
    // los requisitos de capacidad.
    tendererQualificationRequest.setDescription(
        MapperStringFromList.getStringFromListDescriptionType(
            tendererQualificationRequestType.getDescription()));

    Optional.ofNullable(tendererQualificationRequestType.getEmployeeQuantity())
        .ifPresent(employeeQuantityType -> tendererQualificationRequest.setEmployeeQuantity(
            employeeQuantityType.getValue()));

    tendererQualificationRequest.setEmployeeQuantityDescription(
        MapperStringFromList.getStringFromListEmployeeQuantityDescriptionType(
            tendererQualificationRequestType.getEmployeeQuantityDescription()));

    //
    List<EvaluationCriteria> listTechnicalEvaluationcriteria =
        MapperEvaluationCriteria.getListEvaluationCriteria(
            tendererQualificationRequest,
            tendererQualificationRequestType.getTechnicalEvaluationCriteria(),
            TipoSolvencia.TECNICA);

    //
    List<EvaluationCriteria> listFinancialEvaluationcriteria =
        MapperEvaluationCriteria.getListEvaluationCriteria(
            tendererQualificationRequest,
            tendererQualificationRequestType.getFinancialEvaluationCriteria(),
            TipoSolvencia.ECONOMICA);

    //
    List<EvaluationCriteria> listEvaluationCriteria = new ArrayList<>(listTechnicalEvaluationcriteria);
    listEvaluationCriteria.addAll(listFinancialEvaluationcriteria);
    tendererQualificationRequest.setEvaluationCriteria(listEvaluationCriteria);

    //
    Optional.ofNullable(tendererQualificationRequestType.getRequiredBusinessClassificationScheme())
        .filter(list -> !list.isEmpty())
        .ifPresent(list -> tendererQualificationRequest.setRequiredBusinessClassificationScheme(
            MapperClassificationScheme.getListClassificationScheme(
                tendererQualificationRequest, list)));

    Optional.ofNullable(tendererQualificationRequestType.getSpecificTendererRequirement())
        .filter(list -> !list.isEmpty())
        .ifPresent(list -> tendererQualificationRequest.setSpecificTendererRequirement(
            MapperTendererRequirement.getListTendererRequirement(
                tendererQualificationRequest, list)));

    //
    return tendererQualificationRequest;
  }
}
