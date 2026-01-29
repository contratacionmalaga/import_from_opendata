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

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad que representa una referencia documental adicional asociada a una publicación adicional,
 * incluyendo información sobre fecha de emisión y tipo de documento.
 *
 * <p>Hereda campos auditables comunes (creación, modificación).</p>
 *
 * <b>Autor:</b> Juan Antonio
 * <b>Fecha:</b> 06/07/2024
 * <b>Equipo:</b> Juan Antonio
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "additional_publication_document_reference")
public class AdditionalPublicationDocumentReference extends AuditableCreatedAt {

  /**
   * Identificador único generado automáticamente como UUID.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "issue_date")
  private LocalDate issueDate;

  @Column(name = "document_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String documentTypeCode;

  @Column(name = "filename", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String filename;

  @Column(name = "document_hash", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String documentHash;

  @Column(name = "uri", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String uri;

  /**
   * Asociación ManyToOne con {@link AdditionalPublicationStatus}.
   * <p>Indica el estado de publicación al que pertenece esta referencia documental adicional.</p>
   * <p>La eliminación en cascada asegura que si se borra el estado de publicación,
   * también se eliminen estas referencias.</p>
   */
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "additional_publication_status_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_addpubdocref_additionalpubstatus",
          foreignKeyDefinition = "FOREIGN KEY (additional_publication_status_id) REFERENCES additional_publication_status(id) ON DELETE CASCADE"))
  private AdditionalPublicationStatus additionalPublicationStatus;

  /**
   * Representación en texto de la referencia documental adicional, mostrando fecha de emisión y
   * código de tipo de documento.
   *
   * @return cadena con resumen de propiedades relevantes
   */
  @Override
  public String toString() {
    return "AdditionalPublicationDocumentReference: " +
        "[issueDate='" + issueDate + "', " +
        "documentTypeCode='" + documentTypeCode + "']";
  }
}
