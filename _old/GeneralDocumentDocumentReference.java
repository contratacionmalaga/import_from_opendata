package local.jarios.entity.codice;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
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
    name = "general_document_document_reference"
)

public class GeneralDocumentDocumentReference extends AuditableCreatedAt {

  //
  //
  //
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // =========================================================================
  // CLASES PADRES
  // =========================================================================
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "general_document_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_generaldocumentdocumentreference_generaldocument",
          foreignKeyDefinition =
              "FOREIGN KEY (general_document_id) " +
                  "REFERENCES general_document(id) ON DELETE CASCADE"))
  private GeneralDocument generalDocument;

  // =========================================================================
  // CLASES HIJAS
  // =========================================================================
  @OneToOne(mappedBy = "generalDocumentDocumentReference", cascade = CascadeType.ALL, orphanRemoval = true)
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

    return "GeneralDocumentDocumentReference: []";
  }
}
