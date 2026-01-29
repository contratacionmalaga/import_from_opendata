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
 * Representa un archivo adjunto dentro del sistema.
 * <p>
 * Esta entidad extiende de {@link AuditableCreatedAt} para incluir datos de auditoría.
 * </p>
 * <p>
 * Mantiene relaciones uno a uno con {@link DocumentReference},
 * {@link AdditionalPublicationDocumentReference} y {@link ExternalReference}.
 * </p>
 *
 * <p><b>Author:</b> Juan Antonio</p>
 * <p><b>Date:</b> 06/07/2024</p>
 * <p><b>Team:</b> Juan Antonio</p>
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "attachment")
public class Attachment extends AuditableCreatedAt {

  /**
   * Identificador único universal (UUID) del adjunto.
   * <p>
   * Clave primaria generada automáticamente. No puede ser actualizada ni ser nula.
   * </p>
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  // =========================================================================
  // CLASES PADRE
  // =========================================================================
  /**
   * Referencia al documento asociado al adjunto.
   * <p>
   * Relación uno a uno con {@link DocumentReference}. Se aplica cascada completa y carga perezosa.
   * La eliminación en cascada se asegura mediante la clave foránea.
   * </p>
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "document_reference_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_attachment_documentreference",
          foreignKeyDefinition =
              "FOREIGN KEY (document_reference_id) " +
                  "REFERENCES document_reference(id) ON DELETE CASCADE"))
  private DocumentReference documentReference;
  /**
   * Referencia al documento de publicación adicional asociado al adjunto.
   * <p>
   * Relación uno a uno con {@link AdditionalPublicationDocumentReference}. Se aplica cascada
   * completa y carga perezosa. La eliminación en cascada se asegura mediante la clave foránea.
   * </p>
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "additional_publication_document_reference_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_attachment_additionalpublicationdocumentreference",
          foreignKeyDefinition = "FOREIGN KEY (additional_publication_document_reference_id) REFERENCES additional_publication_document_reference(id) ON DELETE CASCADE"))
  private AdditionalPublicationDocumentReference additionalPublicationDocumentReference;
  /**
   * Referencia al documento de publicación adicional asociado al adjunto.
   * <p>
   * Relación uno a uno con {@link AdditionalPublicationDocumentReference}. Se aplica cascada
   * completa y carga perezosa. La eliminación en cascada se asegura mediante la clave foránea.
   * </p>
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "preliminary_market_consultation_status_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_attachment_preliminarymarketconsultationstatus",
          foreignKeyDefinition = "FOREIGN KEY (preliminary_market_consultation_status_id) REFERENCES preliminary_market_consultation_status(id) ON DELETE CASCADE"))
  private PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus;
  // =========================================================================
  // CLASES HIJAS
  // =========================================================================
  /**
   * Referencia externa asociada a este adjunto.
   * <p>Relación uno a uno mapeada por el atributo {@code attachment} en {@link ExternalReference}.
   * Se aplican cascada completa y eliminación de huérfanos.</p>
   */
  @OneToOne(mappedBy = "attachment", cascade = CascadeType.ALL, orphanRemoval = true)
  private ExternalReference externalReference;

  // =========================================================================
  // MÉTODOS AUXILIARES
  // =========================================================================
  /**
   * Devuelve una representación en cadena del objeto con los valores principales de la entidad.
   *
   * @return cadena con el nombre de la clase.
   */
  @Override
  public String toString() {

    return "Attachment: []";
  }
}
