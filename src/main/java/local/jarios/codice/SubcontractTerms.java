package local.jarios.codice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Description: Importaciones de Ficheros Excel desde Internet Author: Juan Antonio Date: 04/06/2024
 * Team: Juan Antonio
 */
@Setter
@Getter
@NoArgsConstructor
// 4.36 Condiciones de subcontratación
// Aporta información si la oferta adjudicataria incluyera subcontratación
public class SubcontractTerms {

  private Double rate;
  private String description;

  @Override
  public String toString() {

    return "SubcontractTerms: " + "[rate='" + rate + "', " + "description='" + description + "']";
  }
}
