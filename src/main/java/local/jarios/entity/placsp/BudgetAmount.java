package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
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

@Setter
@Getter
@Entity
@Table(
        name = "budget_amount"
)
public class BudgetAmount extends Auditable {

    //
    // PROPIEDADES DEL MODELO
    //

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "estimated_overall_contract_amount")
    private Double estimatedOverallContractAmount;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "tax_exclusive_amount")
    private Double taxExclusiveAmount;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "procurement_project_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_procurementproject_budgetamount",
                    foreignKeyDefinition =
                            "FOREIGN KEY (procurement_project_id) " +
                            "REFERENCES procurement_project(id) ON DELETE CASCADE"))
    private ProcurementProject procurementProject;

    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }
    //
    // CONSTRUCTOR DE LA CLASE EN EL QUE SE GENERA EL UUID VERSIÓN 7
    //
    public BudgetAmount() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
