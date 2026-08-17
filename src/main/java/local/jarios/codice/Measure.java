package local.jarios.codice;

import jakarta.persistence.Column;
import java.math.BigDecimal;
import local.jarios.common.util.TamanoCampos;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una medida de duración. */
@Setter
@Getter
@NoArgsConstructor
public class Measure {

  /** Código de la unidad de duración (por ejemplo, "DAY", "MONTH", "YEAR"). */
  @Column(name = "unit_code", length = TamanoCampos.TAMANO_50)
  private String unitCode;

  /** Valor numérico de la duración expresada en la unidad indicada. */
  @Column(name = "value")
  private BigDecimal value;

  /** Devuelve una representación en texto del objeto, útil para depuración. */
  @Override
  public String toString() {

    return "Measure: " + "[unitCode='" + unitCode + "', " + "value='" + value + "']";
  }
}
