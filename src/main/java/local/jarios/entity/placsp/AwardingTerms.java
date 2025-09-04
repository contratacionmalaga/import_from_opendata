package local.jarios.entity.placsp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa los términos de adjudicación en un proceso de licitación.
 * <p>
 * Esta entidad agrupa los criterios de adjudicación necesarios para otorgar un contrato. Puede
 * estar asociada a términos de licitación específicos y contener una lista de criterios detallados
 * que definen cómo se adjudicará el contrato.
 * </p>
 *
 * <p>Relacionada con la entidad {@link TenderingTerms} que representa los términos generales
 * de la licitación, y con {@link AwardingCriteria} que detalla cada criterio individual.</p>
 *
 * <p><b>Author:</b> Juan Antonio</p>
 * <p><b>Date:</b> 04/06/2024</p>
 * <p><b>Team:</b> Juan Antonio</p>
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "awarding_terms")
public class AwardingTerms extends Auditable {

  /**
   * Identificador único universal (UUID) de los términos de adjudicación.
   * <p>
   * Clave primaria generada automáticamente. No puede ser actualizada ni ser nula.
   * </p>
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  /**
   * Referencia a los términos de licitación asociados a estos términos de adjudicación.
   * <p>
   * Relación uno a uno con la entidad {@link TenderingTerms}. La eliminación en cascada está
   * definida en la clave foránea.
   * </p>
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "tendering_terms_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_awardingterms_tenderingterms",
          foreignKeyDefinition = "FOREIGN KEY (tendering_terms_id) REFERENCES tendering_terms(id) ON DELETE CASCADE"))
  private TenderingTerms tenderingTerms;
  /**
   * Lista de criterios de adjudicación asociados a estos términos.
   * <p>
   * Relación uno a muchos con la entidad {@link AwardingCriteria}. Los elementos de la lista se
   * eliminan en cascada y son removidos si no están asociados.
   * </p>
   */
  @OneToMany(mappedBy = "awardingTerms", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AwardingCriteria> listAwardingCriteria = new ArrayList<>();

  @Override
  public String toString() {

    return "AwardingTerms: []";
  }
}
