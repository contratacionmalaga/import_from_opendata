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
        name = "awarding_criteria"
)
// 4.33 Criterio de adjudicación
// Criterio que se requiere para adjudicar el contrato en un proceso de licitación.
// También pueden indicarse a nivel de lote.
public class AwardingCriteria extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Código que tipifica el criterio de adjudicación
    @Column(name = "awarding_criteria_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String awardingCriteriaTypeCode;

    // Codigo que identifica el subtipo de criterio
    @Column(name = "awarding_criteria_subtype_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String awardingCriteriaSubTypeCode;

    // Descripción textual del criterio de adjudicación
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Detalles relativos al criterio de adjudicación
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Valor asignado al cumplimiento de este criterio de adjudicación en el proceso de licitación.
    @Column(name = "weight_numeric")
    private Double weightNumeric;

    //
    //
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "awarding_terms_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_awardingcriteria_awardingterms",
                    foreignKeyDefinition =
                            "FOREIGN KEY (awarding_terms_id) " +
                                    "REFERENCES awarding_terms(id) ON DELETE CASCADE"))
    private AwardingTerms awardingTerms;

    @Override
    public String toString() {

        return "AwardingCriteria: " +
                "[awardingCriteriaTypeCode='" + awardingCriteriaTypeCode + "', " +
                "awardingCriteriaSubTypeCode='" + awardingCriteriaSubTypeCode + "', " +
                "description='" + description + "', " +
                "note='" + note + "', " +
                "weightNumeric='" + weightNumeric + "']";
    }

    //
    //
    //
    public AwardingCriteria() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
