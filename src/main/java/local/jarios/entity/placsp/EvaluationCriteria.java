package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.enums.TipoSolvencia;
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
        name = "evaluation_criteria"
)

// 4.30.1 Criterio de evaluación técnica
// Criterios técnicos requeridos a un operador económico para evaluar su solvencia técnica y determinar su
// capacidad de participar en este contrato. También puede indicarse a nivel de lote
public class EvaluationCriteria extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Código que tipifica el criterio de solvencia.
    @Column(name = "evaluation_criteria_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String evaluationCriteriaTypeCode;

    // Descripción del objeto de la subcontratación
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    //
    @Column(name = "threshold_quantity")
    private Double thresholdQuantity;

    //
    @Enumerated(EnumType.STRING)
    @Column (name = "tipo_solvencia")
    private TipoSolvencia tipoSolvencia;

    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenderer_qualification_request_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_evaluationcriteria_tendererqualificationrequest",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tenderer_qualification_request_id) " +
                            "REFERENCES tenderer_qualification_request(id) ON DELETE CASCADE"))
    private TendererQualificationRequest tendererQualificationRequest;

    @Override
    public String toString() {

        return "EvaluationCriteria: " +
                "[evaluationCriteriaTypeCode='" + evaluationCriteriaTypeCode + "', " +
                "description='" + description + "', " +
                "thresholdQuantity='" + thresholdQuantity + "', " +
                "tipoSolvencia='" + tipoSolvencia + "']";
    }

    public EvaluationCriteria() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
