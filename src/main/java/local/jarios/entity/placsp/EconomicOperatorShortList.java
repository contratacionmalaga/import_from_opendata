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

// Criterios objetivos para la selección del número limitado de candidatos
// Descripción textual de los criterios objetivos para la selección del número limitado de candidatos

@Setter
@Getter
@Entity
@Table(
        name = "economic_operator_short_list"
)
public class EconomicOperatorShortList extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Número previsto de operadores económicos para la creación de la lista corta de candidatos
    @Column(name = "expected_quantity")
    private Double expectedQuantity;

    // Número máximo de empresarios u operadores económicos para la creación de la lista corta de candidatos
    @Column(name = "maximum_quantity")
    private Double maximumQuantity;

    // Número mínimo de empresarios u operadores económicos para la creación de la lista corta de candidatos
    @Column(name = "minimum_quantity")
    private Double minimumQuantity;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tendering_process_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_tenderingprocess_economicoperatorshortlist",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tendering_process_id) " +
                            "REFERENCES tendering_process(id) ON DELETE CASCADE"))
    private TenderingProcess tenderingProcess;

    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }
    //
    //
    //
    public EconomicOperatorShortList() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
