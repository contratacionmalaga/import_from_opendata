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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa el estado adicional de publicación en el proceso de importación de ficheros Excel
 * desde Internet.
 * <p>
 * Esta entidad contiene información sobre el medio de publicación y mantiene las relaciones con la
 * información del aviso relacionado, así como con las solicitudes y referencias de documentos de
 * publicación asociadas.
 * </p>
 * <p>
 * Hereda campos auditables comunes de {@link AuditableCreatedAt}.
 * </p>
 *
 * @author Juan Antonio
 * @since 04/06/2024
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "additional_publication_status")
public class AdditionalPublicationStatus extends AuditableCreatedAt {

  /**
   * Identificador único universal (UUID) del estado de publicación.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  /**
   * Nombre del medio de publicación.
   */
  @Column(name = "publication_media_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String publicationMediaName;
  /**
   * Información del aviso asociado a este estado de publicación.
   */
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "notice_info_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_additionalpublicationstatus_noticeinfo",
          foreignKeyDefinition = "FOREIGN KEY (notice_info_id) REFERENCES notice_info(id) ON DELETE CASCADE"))
  private NoticeInfo noticeInfo;

//  /**
//   * Lista de solicitudes de publicación adicionales asociadas a este estado.
//   */
//  @OneToMany(mappedBy = "additionalPublicationStatus", cascade = CascadeType.ALL, orphanRemoval = true)
//  private List<AdditionalPublicationRequest> additionalPublicationRequestList = new ArrayList<>();
  /**
   * Lista de referencias de documentos de publicación adicionales asociadas a este estado.
   */
  @OneToMany(mappedBy = "additionalPublicationStatus", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AdditionalPublicationDocumentReference> additionalPublicationDocumentReferenceList = new ArrayList<>();

  /**
   * Representación textual de la entidad.
   * <p>
   * Devuelve una cadena con el nombre del medio de publicación para facilitar la depuración y el
   * registro en logs.
   * </p>
   *
   * @return Cadena con el campo {@code publicationMediaName}.
   */
  @Override
  public String toString() {
    return "AdditionalPublicationStatus: " +
        "[publicationMediaName='" + publicationMediaName + "']";
  }
}
