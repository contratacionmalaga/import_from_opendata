package local.jarios.entity.codice;

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
 * Entidad que representa los términos de subasta electrónica asociados a un proceso de licitación.
 *
 * <p>Permite almacenar si se aplicará una subasta electrónica en un proceso de adjudicación.</p>
 *
 * <p>Relacionado uno a uno con {@link TenderingProcess} y hereda campos auditables como
 * fecha de creación, modificación y usuario.</p>
 *
 * @author Juan
 * @since 06/07/2024
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "auction_terms")
public class AuctionTerms extends AuditableCreatedAt {

  /**
   * Identificador único de la entidad. Se genera automáticamente como UUID.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  /**
   * Indicador de subasta electrónica.
   * <p>Determina si se usará una subasta electrónica como parte del proceso de adjudicación del
   * contrato.</p>
   */
  @Column(name = "auction_constraint_indicator")
  private Boolean auctionConstraintIndicator;
  /**
   * Relación uno a uno con el proceso de licitación asociado.
   * <p>Si se elimina el proceso, también se elimina esta entidad gracias a
   * {@code ON DELETE CASCADE}.</p>
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "tenderin_gprocess_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_auctionterms_tenderingprocess",
          foreignKeyDefinition = "FOREIGN KEY (tenderin_gprocess_id) REFERENCES tendering_process(id) ON DELETE CASCADE")
  )
  private TenderingProcess tenderingProcess;

  @Override
  public String toString() {

    return "AuctionTerms: []";
  }
}
