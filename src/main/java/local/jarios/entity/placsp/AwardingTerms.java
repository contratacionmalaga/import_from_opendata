package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
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
        name = "awarding_terms"
)

// 4.33 Criterio de adjudicación
// Criterio que se requiere para adjudicar el contrato en un proceso de licitación.
// También pueden indicarse a nivel de lote.
public class AwardingTerms extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tendering_terms_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_awardingterms_tenderingterms",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tendering_terms_id) " +
                            "REFERENCES tendering_terms(id) ON DELETE CASCADE"))
    private TenderingTerms tenderingTerms;

    //
    //
    //
    @OneToMany(mappedBy = "awardingTerms", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AwardingCriteria> listAwardingCriteria = new ArrayList<>();

    //
    //
    //
    public AwardingTerms() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
