package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Setter
@Getter
@Entity
@Table(
        name = "auction_terms"
)
public class AuctionTerms extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // 4.40 Usa Subasta Electrónica
    // Permite indicar si se va a recurrir a una subasta electrónica para adjudicar el contrato
    @Column(name = "auction_constraint_indicator")
    private Boolean auctionConstraintIndicator;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenderin_gprocess_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_tenderingprocess_auctionterms",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tenderin_gprocess_id) " +
                            "REFERENCES tendering_process(id) ON DELETE CASCADE"))
    private TenderingProcess tenderingProcess;

    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }
    //
    //
    //
    public AuctionTerms() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
