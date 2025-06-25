package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Description: Importaciones de Ficheros Excel desde Internet
 * Author: Juan Antonio
 * Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@Entity
@Table(
        name = "financial_guarantee"
)

// 4.29 Garantías requeridas
public class FinancialGuarantee extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Tipo de Garantía
    @Column(name = "guarantee_type_code", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String guaranteeTypeCode;

    // Porcentaje de la garantía
    @Column(name = "amount_rate")
    private Double amountRate;

    // Importe
    @Column(name = "liability_amount")
    private Double liabilityAmount;

    //
    //
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tendering_terms_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_financialguarantee_tenderingterms",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tendering_terms_id) " +
                            "REFERENCES tendering_terms(id) ON DELETE CASCADE"))
    private TenderingTerms tenderingTerms;

    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }

    //
    //
    //
    public FinancialGuarantee() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
