package local.jarios.entity.codice;

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
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad que representa una modificación contractual dentro del ciclo de vida de un contrato
 * público.
 * <p>
 * Una modificación de contrato puede afectar tanto a las condiciones económicas como a las
 * condiciones de duración, así como contener información adicional sobre el contrato original y sus
 * lotes.
 * </p>
 *
 * <p>
 * Cada registro se corresponde con un evento de modificación en la tabla
 * <b>contract_modification</b>.
 * </p>
 *
 * <p>
 * Hereda de {@link AuditableCreatedAt}, por lo que incluye los metadatos de auditoría (fecha de
 * creación, última modificación, usuario, etc.).
 * </p>
 *
 * @author Juan
 * @version 1.0
 * @since 04/06/2024
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "contract_modification")
public class ContractModification extends AuditableCreatedAt {

  /**
   * Identificador único de la modificación contractual en formato UUID. Se genera automáticamente
   * al persistir la entidad.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  /**
   * Identificador del contrato al que aplica la modificación.
   */
  @Column(name = "contract_id", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String contractId;
  /**
   * Identificador de la modificación del contrato.
   */
  @Column(name = "contract_modification_id", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String contractModificationId;
  /**
   * Fecha en la que se formalizó la modificación del contrato.
   */
  @Column(name = "issue_date")
  private LocalDate issueDate;
  /**
   * Notas o comentarios adicionales asociados a la modificación contractual.
   */
  @Column(name = "note", columnDefinition = "TEXT")
  private String note;

  @Column(name = "contract_modification_tax_exclusive_amount")
  private Double contractModificationTaxExclusiveAmount;

  @Column(name = "final_tax_exclusive_amount")
  private Double finalTaxExclusiveAmount;

  @Column(name = "contract_modification_measure_value")
  private BigDecimal contractModificationMeasureValue;

  @Column(name = "contract_modification_measure_unit_code")
  private String contractModificationMeasureUnitCode;

  @Column(name = "final_measure_value")
  private BigDecimal finalMeasureValue;

  @Column(name = "final_measure_unit_code")
  private String finalMeauseUnitCode;

  // =========================================================================
  // RELACIONES PADRES
  // =========================================================================
  /**
   * Estado del expediente de contratación al que está vinculada la modificación.
   * <p>
   * Relación muchos-a-uno con {@link ContractFolderStatus}. Incluye eliminación en cascada para
   * mantener integridad.
   * </p>
   */
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contract_folder_status_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_contractmodification_contractfolderstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (contract_folder_status_id) " +
                  "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus contractFolderStatus;


  // =========================================================================
  // OTROS MÉTODOS
  // =========================================================================

  /**
   * Devuelve una representación en cadena de la modificación contractual, mostrando los valores
   * principales de sus atributos.
   *
   * @return cadena con los valores de {@code contractId}, {@code issueDate}, {@code note},
   * {@code contractModificationLotId} y {@code idContractModification}.
   */
  @Override
  public String toString() {
    return "ContractModification: " +
        "[contractId='" + contractId + "', " +
        "issueDate='" + issueDate + "', " +
        "note='" + note + "', " + "']";
  }
}
