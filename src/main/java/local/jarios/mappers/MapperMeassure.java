package local.jarios.mappers;

import local.jarios.entity.placsp.ContractModification;
import local.jarios.entity.placsp.Measure;
import local.jarios.entity.placsp.Period;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.cbclib.DurationMeasureType;
import org.dgpe.codice.common.cbclib.MeasureType;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Description: Juan Antonio Author: juan Date: 06/07/2024 Team: Juan Antonio
 */
@Slf4j
public final class MapperMeassure {

  private MapperMeassure() {
  }

  public static Measure getMeasure(
      Period period,
      ContractModification contractModification,
      MeasureType measureType) {

    return buildMeasure(period, contractModification,
                        () -> measureType == null ? null : measureType.getUnitCode(),
                        () -> measureType == null ? null : measureType.getValue());
  }

  public static Measure getDurationMeasure(
      Period period,
      ContractModification contractModification,
      DurationMeasureType durationMeasureType) {

    return buildMeasure(period, contractModification,
                        () -> durationMeasureType == null ? null : durationMeasureType.getUnitCode(),
                        () -> durationMeasureType == null ? null : durationMeasureType.getValue());
  }

  private static Measure buildMeasure(
      Period period,
      ContractModification contractModification,
      Supplier<String> unitCodeSupplier,
      Supplier<BigDecimal> valueSupplier) {

    if (unitCodeSupplier == null && valueSupplier == null) {
      return null;
    }

    Measure measure = new Measure();
    measure.setPeriod(period);
    measure.setContractModificationFinalDurationMeasure(contractModification);

    if (unitCodeSupplier != null) {
      Optional.ofNullable(unitCodeSupplier.get()).ifPresent(measure::setUnitCode);
    }

    if (valueSupplier != null) {
      Optional.ofNullable(valueSupplier.get()).ifPresent(measure::setValue);
    }

    return measure;
  }
}
