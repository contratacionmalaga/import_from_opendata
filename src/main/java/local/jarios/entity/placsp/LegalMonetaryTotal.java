package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Description:
 * Author: juan
 * Date: 20/03/2025
 * Team:
 */

// Importe de adjudicación.
//  Importe ofertado por el licitador adjudicatario del contrato sin impuestos y con impuestos
// CARDINALIDAD 0..n. Se podrán indicar importes de adjudicación distintos para cada adjudicatario,
//     y si hubiera lotes para cada adjudicatario en cada lote adjudicado.

@Setter
@Getter
@Entity
@Table(
        name = "legal_monetary_total"
)
public class LegalMonetaryTotal extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "payable_amount")
    private Double payableAmount;

    @Column(name = "tax_exclusive_amount")
    private Double taxExclusiveAmount;

    @Column(name = "tax_inclusive_amount")
    private Double taxInclusiveAmount;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "awarded_tendered_project_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_awardedtenderedproject_legalmonetarytotal",
                    foreignKeyDefinition =
                            "FOREIGN KEY (awarded_tendered_project_id) " +
                            "REFERENCES tendered_project(id) ON DELETE CASCADE"))
    private TenderedProject awardedTenderedProject;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_modification_legal_monetary_total_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_contractmodification_legalmonetarytotal",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_modification_legal_monetary_total_id) " +
                            "REFERENCES contract_modification(id) ON DELETE CASCADE"))
    private ContractModification contractModificationLegalMonetaryTotal;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_modification_final_legal_monetary_total_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_contractmodificationfinal_legalmonetarytotal",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_modification_final_legal_monetary_total_id) " +
                            "REFERENCES contract_modification(id) ON DELETE CASCADE"))
    private ContractModification contractModificationFinalLegalMonetaryTotal;

    @Override
    public String toString() {

        return "LegalMonetaryTotal: " +
                "[payableAmount='" + payableAmount + "', " +
                "taxExclusiveAmount='" + taxExclusiveAmount + "', " +
                "taxInclusiveAmount='" + taxInclusiveAmount + "']";
    }
}
