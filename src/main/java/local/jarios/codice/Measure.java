package local.jarios.codice;

import jakarta.persistence.Column;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad que representa una medida de duración.
 */
@Setter
@Getter
@NoArgsConstructor
public class Measure {

  /**
   * Código de la unidad de duración (por ejemplo, "DAY", "MONTH", "YEAR").
   */
  @Column(name = "unit_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String unitCode;

  /**
   * Valor numérico de la duración expresada en la unidad indicada.
   */
  @Column(name = "value")
  private BigDecimal value;

  /**
   * Devuelve una representación en texto del objeto, útil para depuración.
   */
  @Override
  public String toString() {

    return "Measure: " +
        "[unitCode='" + unitCode + "', " +
        "value='" + value + "']";
  }
}
