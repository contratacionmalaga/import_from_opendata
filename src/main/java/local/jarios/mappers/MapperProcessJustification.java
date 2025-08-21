package local.jarios.mappers;

import local.jarios.entity.placsp.ProcessJustification;
import local.jarios.entity.placsp.TenderingProcess;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ProcessJustificationType;
import org.dgpe.codice.common.cbclib.ReasonCodeType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperProcessJustification {

  private MapperProcessJustification() {
  }

  public static List<ProcessJustification> getListProcessJustificationFromType(
      TenderingProcess tenderingProcess,
      List<ProcessJustificationType> listProcessJustificationType) {

    return Optional.ofNullable(listProcessJustificationType)
        .orElseGet(List::of) // evitar NPE si la lista es null
        .stream()
        .map(processJustificationType ->
            getProcessJustification(tenderingProcess, processJustificationType))
        .toList();
  }

  private static ProcessJustification getProcessJustification(
      TenderingProcess tenderingProcess,
      ProcessJustificationType processJustificationType) {

    ProcessJustification processJustification = new ProcessJustification();

    processJustification.setTenderingProcess(tenderingProcess);

    Optional.ofNullable(processJustificationType.getReasonCode())
        .map(ReasonCodeType::getValue)
        .ifPresent(processJustification::setReasonCode);

    processJustification.setDescription(
        StringHelper.eliminarCaracteres(
            MapperStringFromList.getStringFromListDescriptionType(
                processJustificationType.getDescription())));

    return processJustification;
  }
}
