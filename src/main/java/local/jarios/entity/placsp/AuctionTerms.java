package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
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
@Entity
@Table(name = "auction_terms")
public class AuctionTerms extends Auditable {

    /**
     * Identificador único de la entidad. Se genera automáticamente como UUID.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Indicador de subasta electrónica.
     * <p>Determina si se usará una subasta electrónica como parte del proceso de adjudicación del contrato.</p>
     */
    @Column(name = "auction_constraint_indicator")
    private Boolean auctionConstraintIndicator;

    /**
     * Relación uno a uno con el proceso de licitación asociado.
     * <p>Si se elimina el proceso, también se elimina esta entidad gracias a {@code ON DELETE CASCADE}.</p>
     */
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenderin_gprocess_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_tenderingprocess_auctionterms",
                    foreignKeyDefinition = "FOREIGN KEY (tenderin_gprocess_id) REFERENCES tendering_process(id) ON DELETE CASCADE")
    )
    private TenderingProcess tenderingProcess;

    /**
     * Devuelve una representación en texto de esta entidad.
     * Utiliza {@link ToStringUtil#autoToString(Object)} para generar dinámicamente los campos.
     *
     * @return representación textual del objeto
     */
    @Override
    public String toString() {
        return ToStringUtil.autoToString(this);
    }
}
