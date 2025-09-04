package local.jarios.entity.placsp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Description: Importaciones de Ficheros Excel desde Internet Author: Juan Antonio Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "process_justification"
)
public class ProcessJustification extends Auditable {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Código del motivo: Código que tipifica el motivo por los que se seleccionó el
  //     procedimiento extraordinario de contratación.
  // La lista de códigos se encuentra en
  //         [...](http://contrataciondelestado.es/codice/cl/2.0/ProcessJustificationReasonCode-2.0.gc)
  @Column(name = "reason_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String reasonCode;

  // Descripción: Descripción textual de la jusitificación de la utilización de un determinado
  //     procedimiento de contratación.
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  // =========================================================================
  // RELACIONES PADRES
  // =========================================================================
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "tendering_process_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_processjustification_tenderingprocess",
          foreignKeyDefinition =
              "FOREIGN KEY (tendering_process_id) " +
                  "REFERENCES tendering_process(id) ON DELETE CASCADE"))
  private TenderingProcess tenderingProcess;

  // =========================================================================
  // OTROS MÉTODOS
  // =========================================================================
  /**
   * Devuelve una representación en cadena del objeto con los valores principales de la entidad.
   *
   * @return cadena con los valores de {@code reasonCode} y {@code description}.
   */
  @Override
  public String toString() {

    return "ProcessJustification: " +
        "[reasonCode='" + reasonCode + "', " +
        "description='" + description + "']";
  }
}
