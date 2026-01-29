package local.jarios.codice;

import org.dgpe.codice.common.cbclib.ContractModificationDurationMeasureType;
import org.dgpe.codice.common.cbclib.FinalDurationMeasureType;
import org.dgpe.codice.common.cbclib.MeasureType;

/**
 * Description: Juan Antonio Author: juan Date: 06/07/2024 Team: Juan Antonio
 */
public final class MeassureMapper {

  private MeassureMapper() {
  }

  public static Measure getMeasure(MeasureType measureType) {

    Measure measure = new Measure();

    measure.setUnitCode(measureType.getUnitCode());
    measure.setValue(measureType.getValue());

    return measure;
  }

  public static Measure getMeasure(ContractModificationDurationMeasureType measureType) {

    Measure measure = new Measure();

    measure.setUnitCode(measureType.getUnitCode());
    measure.setValue(measureType.getValue());

    return measure;
  }

  public static Measure getMeasure(FinalDurationMeasureType measureType) {

    Measure measure = new Measure();

    measure.setUnitCode(measureType.getUnitCode());
    measure.setValue(measureType.getValue());

    return measure;
  }
}
