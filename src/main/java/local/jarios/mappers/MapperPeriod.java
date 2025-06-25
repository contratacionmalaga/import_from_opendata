package local.jarios.mappers;

import local.jarios.entity.placsp.DurationMeasure;
import local.jarios.entity.placsp.*;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.FechaHelper;
import local.jarios.helpers.GregorianCalendarHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.PeriodType;

import java.sql.Date;
import java.sql.Time;
import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperPeriod {

    private MapperPeriod() { }

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
                .ifPresent(startDateType -> {
                    Date startDate = GregorianCalendarHelper
                            .getDateFromXMLGregorianCalendar(startDateType.getValue());
                    Time startTime = Optional.ofNullable(periodType.getStartTime())
                            .map(startTimeType -> GregorianCalendarHelper
                                    .getTimeFromXMLGregorianCalendar(startTimeType.getValue()))
                            .orElse(null);
                    period.setStartDateTime(FechaHelper.getLocalDateTime(startDate, startTime));
                });

        // EndDate + EndTime
        Optional.ofNullable(periodType.getEndDate())
                .ifPresent(endDateType -> {
                    Date endDate = GregorianCalendarHelper
                            .getDateFromXMLGregorianCalendar(endDateType.getValue());
                    Time endTime = Optional.ofNullable(periodType.getEndTime())
                            .map(endTimeType -> GregorianCalendarHelper
                                    .getTimeFromXMLGregorianCalendar(endTimeType.getValue()))
                            .orElse(null);
                    period.setEndDateTime(FechaHelper.getLocalDateTime(endDate, endTime));
                });

        // Description
        period.setDescription(
                StringHelper.eliminarCaracteres(
                        MapperStringFromList.getStringFromListDescriptionType(
                                periodType.getDescription())));

        // Duration Measure
        Optional.ofNullable(periodType.getDurationMeasure())
                .ifPresent(durationMeasureType -> {
                    DurationMeasure durationMeasure = MapperDurationMeasure.getDurationMeasure(durationMeasureType);
                    period.setDurationMeasureUnitCode(
                            ComunHelper.limitarRegistro(
                                    durationMeasure.getUnitCode(),
                                    Constantes.TAMANO_MAXIMO_CAMPO_5));
                    period.setDurationMeasureValue(durationMeasure.getValue());
                });

        log.debug(period.toString());
        return period;
    }
}
