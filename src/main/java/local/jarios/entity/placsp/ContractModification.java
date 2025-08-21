package local.jarios.entity.placsp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad que representa una modificación contractual dentro del ciclo de vida
 * de un contrato público.
 * <p>
 * Una modificación de contrato puede afectar tanto a las condiciones económicas
 * como a las condiciones de duración, así como contener información adicional
 * sobre el contrato original y sus lotes.
 * </p>
 *
 * <p>
 * Cada registro se corresponde con un evento de modificación en la tabla
 * <b>contract_modification</b>.
 * </p>
 *
 * <p>
 * Hereda de {@link Auditable}, por lo que incluye los metadatos de auditoría
 * (fecha de creación, última modificación, usuario, etc.).
 * </p>
 *
 * @author Juan
 * @version 1.0
 * @since 04/06/2024
 */
@Setter
@Getter
@Entity
@Table(name = "contract_modification")
public class ContractModification extends Auditable {

  /**
   * Identificador único de la modificación contractual en formato UUID.
   * Se genera automáticamente al persistir la entidad.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  /**
   * Identificador del contrato al que aplica la modificación.
   * <p>
   * Campo obligatorio, limitado a
   * {@link Constantes#TAMANO_MAXIMO_CAMPO_50} caracteres.
   * </p>
   */
  @Column(name = "contract_id", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String contractId;
  /**
   * Identificador de la modificación del contrato.
   * <p>
   * Un mismo contrato puede tener múltiples modificaciones,
   * por lo que este campo permite diferenciarlas.
   * </p>
   */
  @Column(name = "id_contract_modification", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String idContractModification;
  /**
   * Fecha en la que se formalizó la modificación del contrato.
   */
  @Column(name = "issue_date")
  private LocalDate issueDate;
  /**
   * Notas o comentarios adicionales asociados a la modificación contractual.
   * <p>
   * Se almacena como texto largo ({@code TEXT} en la base de datos).
   * </p>
   */
  @Column(name = "note", columnDefinition = "TEXT")
  private String note;
  /**
   * Identificador del lote afectado por la modificación contractual,
   * en caso de que la licitación se realice por lotes.
   */
  @Column(name = "contract_modification_lot_id", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String contractModificationLotId;
  /**
   * Estado del expediente de contratación al que está vinculada
   * la modificación.
   * <p>
   * Relación muchos-a-uno con {@link ContractFolderStatus}.
   * Incluye eliminación en cascada para mantener integridad.
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
  /**
   * Importe sin impuestos de la modificación.
   * <p>
   * Puede ser positivo (incremento) o negativo (decremento).
   * Relación uno-a-uno con {@link LegalMonetaryTotal}.
   * </p>
   */
  @OneToOne(mappedBy = "contractModificationLegalMonetaryTotal", cascade = CascadeType.ALL, orphanRemoval = true)
  private LegalMonetaryTotal contractModificationLegalMonetaryTotal;
  /**
   * Importe sin impuestos del contrato tras la modificación.
   * <p>
   * Se calcula como el importe inicial más todas las modificaciones.
   * Relación uno-a-uno con {@link LegalMonetaryTotal}.
   * </p>
   */
  @OneToOne(mappedBy = "contractModificationFinalLegalMonetaryTotal", cascade = CascadeType.ALL, orphanRemoval = true)
  private LegalMonetaryTotal contractModificationFinalLegalMonetaryTotal;
  /**
   * Duración final del contrato tras la modificación,
   * expresada como una medida temporal.
   * Relación uno-a-uno con {@link Measure}.
   */
  @OneToOne(mappedBy = "contractModification", cascade = CascadeType.ALL, orphanRemoval = true)
  private Measure finalDurationMeasure;

  /**
   * Constructor por defecto.
   * <p>
   * Requerido por JPA para la correcta creación de proxies
   * y por Lombok para la inicialización básica.
   * </p>
   */
  public ContractModification() {
    // Constructor vacío requerido por JPA
  }

  /**
   * Devuelve una representación en cadena de la modificación contractual,
   * mostrando los valores principales de sus atributos.
   *
   * @return cadena con los valores de {@code contractId},
   * {@code issueDate}, {@code note}, {@code contractModificationLotId}
   * y {@code idContractModification}.
   */
  @Override
  public String toString() {
    return "ContractModification: " +
        "[contractId='" + contractId + "', " +
        "issueDate='" + issueDate + "', " +
        "note='" + note + "', " +
        "contractModificationLotId='" + contractModificationLotId + "', " +
        "idContractModification='" + idContractModification + "']";
  }
}
