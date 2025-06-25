package local.jarios.mappers;

import local.jarios.entity.placsp.DurationMeasure;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.cbclib.DurationMeasureType;
import org.dgpe.codice.common.cbclib.FinalDurationMeasureType;

import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperDurationMeasure {

    private MapperDurationMeasure() { }

    public static DurationMeasure getDurationMeasure(Object type) {
        DurationMeasure durationMeasure = new DurationMeasure();

        if (type instanceof DurationMeasureType dmt) {
            Optional.ofNullable(dmt.getUnitCode()).ifPresent(durationMeasure::setUnitCode);
            Optional.ofNullable(dmt.getValue()).map(Number::doubleValue).ifPresent(durationMeasure::setValue);
        } else if (type instanceof FinalDurationMeasureType fdmt) {
            Optional.ofNullable(fdmt.getUnitCode()).ifPresent(durationMeasure::setUnitCode);
            Optional.ofNullable(fdmt.getValue()).map(Number::doubleValue).ifPresent(durationMeasure::setValue);
        }

        return durationMeasure;
    }
}
