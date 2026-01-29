package local.jarios.codice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Description: Author: juan Date: 20/03/2025 Team:
 */

// Importe de adjudicación.
//  Importe ofertado por el licitador adjudicatario del contrato sin impuestos y con impuestos
// CARDINALIDAD 0..n. Se podrán indicar importes de adjudicación distintos para cada adjudicatario,
//     y si hubiera lotes para cada adjudicatario en cada lote adjudicado.

@Setter
@Getter
@NoArgsConstructor
public class LegalMonetaryTotal {

  private Double payableAmount;
  private Double taxExclusiveAmount;
  private Double taxInclusiveAmount;

  @Override
  public String toString() {

    return "LegalMonetaryTotal: " +
        "[payableAmount='" + payableAmount + "', " +
        "taxExclusiveAmount='" + taxExclusiveAmount + "', " +
        "taxInclusiveAmount='" + taxInclusiveAmount + "']";
  }
}
