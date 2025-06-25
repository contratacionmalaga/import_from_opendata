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
        name = "contract_execution_requirement"
)

// 4.48 Condiciones especiales de ejecución del contrato
// Se indican una serie de condiciones especiales sobre la ejecución del contrato
public class ContractExecutionRequirement extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Descripción
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Código de la Condición especial de ejecución
    @Column(name = "execution_requirement_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String executionRequirementCode;

    // Nombre de la Condición especial de ejecución
    @Column(name = "name", columnDefinition = "TEXT")
    private String name;

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
                    name = "fk_contractexecutionrequirements_tenderingterms",
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
    public ContractExecutionRequirement() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
