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
import java.time.LocalDateTime;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una referencia documental adicional asociada a una publicación adicional,
 * incluyendo información sobre fecha de emisión y tipo de documento.
 *
 * <p>Hereda campos auditables comunes (creación, modificación). <b>Autor:</b> Juan Antonio
 * <b>Fecha:</b> 06/07/2024 <b>Equipo:</b> Juan Antonio
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "additional_publication_request")
public class AdditionalPublicationRequest extends AuditableCreatedAt {

  /** Identificador único generado automáticamente como UUID. */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "agency_id", nullable = false, length = TamanoCampos.TAMANO_500)
  private String agencyId;

  @Column(name = "send_date_time")
  private LocalDateTime sendDateTime;

  @Column(name = "note", columnDefinition = "TEXT")
  private String note;

  /**
   * Asociación ManyToOne con {@link AdditionalPublicationStatus}.
   *
   * <p>Indica el estado de publicación al que pertenece esta referencia documental adicional.
   *
   * <p>La eliminación en cascada asegura que si se borra el estado de publicación, también se
   * eliminen estas referencias.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "additional_publication_status_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_apr_aps",
              foreignKeyDefinition =
                  "FOREIGN KEY (additional_publication_status_id) REFERENCES additional_publication_status(id) ON DELETE CASCADE"))
  private AdditionalPublicationStatus additionalPublicationStatus;
}
