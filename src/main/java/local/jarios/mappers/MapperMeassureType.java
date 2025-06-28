package local.jarios.mappers;

import local.jarios.entity.placsp.ContractModification;
import local.jarios.entity.placsp.Measure;
import local.jarios.entity.placsp.Period;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.cbclib.MeasureType;

import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperMeassureType {

    private MapperMeassureType() { }

    public static Measure getMeasure(
            Period period,
            ContractModification contractModification,
            MeasureType measureType) {

        if (measureType == null) {
            return null;
        }

        Measure measure = new Measure();
        measure.setPeriod(period);
        measure.setContractModification(contractModification);

        Optional.ofNullable(measureType.getUnitCode()).ifPresent(measure::setUnitCode);
        Optional.ofNullable(measureType.getValue()).ifPresent(measure::setValue);

        return measure;
    }
}
