package local.jarios.entity.codice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import local.jarios.enums.TipoDocumento;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidad que representa una referencia documental asociada a la importación de ficheros Excel
 * desde Internet.
 *
 * <p>La clase permite modelar la relación de un documento principal con diferentes tipos de
 * referencias documentales adicionales, técnicas, legales o generales, así como su posible adjunto
 * asociado. Hereda de {@link AuditableCreatedAt}, lo que permite registrar metadatos de auditoría
 * (como fechas de creación, actualización o usuario responsable). Cada instancia de esta entidad se
 * corresponde con un registro en la tabla
 * <b>document_reference</b> de la base de datos.</p>
 *
 * @author Juan Antonio
 * @version 1.0
 * @since 04/06/2024
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "document_reference")
public class DocumentReference extends AuditableCreatedAt {

  /**
   * Identificador único de la entidad en formato UUID. Se genera automáticamente al persistir la
   * entidad.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "document_reference_id", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String documentReferenceId;

  @Column(name = "document_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String documentTypeCode;

  @Column(name = "document_type", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String documentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_documento", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private TipoDocumento tipoDocumento;

  @Column(name = "filename", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String filename;

  @Column(name = "document_hash", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String documentHash;

  @Column(name = "uri", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String uri;

  // =========================================================================
  // RELACIONES PADRES
  // =========================================================================

  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "additional_document_reference_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_docref_additional_cfs",
          foreignKeyDefinition = "FOREIGN KEY (additional_document_reference_id) " +
              "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus additionalDocumentReference;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "technical_document_reference_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_docref_technical_cfs",
          foreignKeyDefinition = "FOREIGN KEY (technical_document_reference_id) " +
              "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus technicalDocumentReference;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "legal_document_reference_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_docref_legal_cfs",
          foreignKeyDefinition = "FOREIGN KEY (legal_document_reference_id) " +
              "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus legalDocumentReference;

  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "general_document_reference_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_docref_general_cfs",
          foreignKeyDefinition = "FOREIGN KEY (general_document_reference_id) " +
              "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus generalDocumentReference;

  // =========================================================================
  // RELACIONES HIJAS
  // =========================================================================

  /**
   * Devuelve una representación en cadena del objeto con los datos principales de la entidad.
   *
   * @return cadena con los valores de {@code idDocumentReference} y {@code documentTypeCode}.
   */
  @Override
  public String toString() {
    return "DocumentReference: [" +
        "docuemntReferenceId='" + documentReferenceId + "', " +
        "documentTypeCode='" + documentTypeCode + "']";
  }
}
