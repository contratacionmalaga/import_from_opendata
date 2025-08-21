package local.jarios.mappers;

import local.jarios.entity.placsp.ContractExtension;
import local.jarios.entity.placsp.Measure;
import local.jarios.entity.placsp.Period;
import local.jarios.entity.placsp.ProcurementProject;
import local.jarios.entity.placsp.TenderingProcess;
import local.jarios.helpers.GregorianCalendarHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.PeriodType;
import org.dgpe.codice.common.cbclib.DurationMeasureType;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperPeriod {

  private MapperPeriod() {
  }

  public static Period getPeriod(
      ProcurementProject procurementProject,
      TenderingProcess tenderingProcess,
      ContractExtension contractExtension,
      PeriodType periodType) {

    //
    Period period = new Period();
    period.setProcurementProject(procurementProject);
    period.setTenderingProcess(tenderingProcess);
    period.setContractExtension(contractExtension);

    // StartDate + StartTime
    Optional.ofNullable(periodType.getStartDate())
        .map(startDateType ->
            GregorianCalendarHelper.getDateFromXMLGregorianCalendar(startDateType.getValue()))
        .ifPresent(startDate -> {
          LocalTime startTime = Optional.ofNullable(periodType.getStartTime())
              .map(startTimeType ->
                  GregorianCalendarHelper.getTimeFromXMLGregorianCalendar(startTimeType.getValue()))
              .orElse(LocalTime.MIDNIGHT);

          period.setStartDateTime(LocalDateTime.of(startDate, startTime));
        });

    // EndDate + EndTime
    Optional.ofNullable(periodType.getEndDate())
        .map(endDateType ->
            GregorianCalendarHelper.getDateFromXMLGregorianCalendar(endDateType.getValue()))
        .ifPresent(endDate -> {
          LocalTime endTime = Optional.ofNullable(periodType.getEndTime())
              .map(endTimeType ->
                  GregorianCalendarHelper.getTimeFromXMLGregorianCalendar(endTimeType.getValue()))
              .orElse(LocalTime.MIDNIGHT);
          period.setEndDateTime(LocalDateTime.of(endDate, endTime));
        });

    // Description
    period.setDescription(
        StringHelper.eliminarCaracteres(
            MapperStringFromList.getStringFromListDescriptionType(
                periodType.getDescription())));

    // Duration Measure
    Optional.ofNullable(periodType.getDurationMeasure())
        .ifPresent(measure ->
            period.setDurationMeasure(
                getMeasureFromDurantionMeasure(
                    period,
                    periodType.getDurationMeasure()
                )
            )
        );

    return period;
  }

  private static Measure getMeasureFromDurantionMeasure(
      Period period, DurationMeasureType durationMeasureType) {

    Measure measure = new Measure();

    measure.setPeriod(period);
    measure.setContractModification(null);

    Optional.ofNullable(durationMeasureType.getValue()).ifPresent(measure::setValue);
    Optional.ofNullable(durationMeasureType.getUnitCode()).ifPresent(measure::setUnitCode);

    return measure;
  }
}
