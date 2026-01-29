package local.jarios.entity.codice;

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
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidad que representa una referencia adicional de documento, típicamente utilizada para importar
 * ficheros Excel desde Internet y asociarlos a un estado de carpeta de contrato.
 *
 * <p>Hereda propiedades auditables comunes como creación y modificación.</p>
 *
 * <b>Autor:</b> Juan Antonio
 * <b>Fecha:</b> 06/07/2024
 * <b>Equipo:</b> Juan Antonio
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "additional_document_reference")
public class AdditionalDocumentReference extends AuditableCreatedAt {

  /**
   * Identificador único de esta entidad, generado automáticamente como UUID.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // =========================================================================
  // CLASES PADRES
  // =========================================================================
  /**
   * Relación de muchos a uno con {@link ContractFolderStatus}.
   * <p>Indica el estado de la carpeta del contrato al que está asociada esta referencia
   * adicional.</p>
   * <p>Al eliminar el estado de carpeta de contrato, se eliminarán las referencias asociadas
   * gracias a {@code ON DELETE CASCADE}.</p>
   */
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contract_folder_status_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_additionaldocumentreference_contractfolderstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (contract_folder_status_id) " +
                  "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus contractFolderStatus;

  // =========================================================================
  // CLASES HIJAS
  // =========================================================================
  /**
   * Relación uno a uno con {@link DocumentReference}.
   * <p>Esta referencia adicional tiene una referencia documental asociada,
   * que se mantiene sincronizada mediante cascada y eliminación en órfano.</p>
   */
  @OneToOne(mappedBy = "additionalDocumentReference", cascade = CascadeType.ALL, orphanRemoval = true)
  private DocumentReference documentReference;

  // =========================================================================
  // OTROS MÉTODOS
  // =========================================================================
  /**
   * Devuelve una representación en cadena del objeto con los valores principales de la entidad.
   *
   * @return cadena con el nombre de la clase.
   */
  @Override
  public String toString() {

    return "AdditionalDocumentReference: []";
  }
}
