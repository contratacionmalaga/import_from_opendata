package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
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
        name = "subcontract_terms"
)

// 4.36 Condiciones de subcontratación
// Aporta información si la oferta adjudicataria incluyera subcontratación
public class SubcontractTerms extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Porcentaje de subcontratación
    @Column(name = "rate")
    private Double rate;

    // Descripción del objeto de la subcontratación
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    //
    //
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tender_result_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_tenderresult_subcontractterms",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tender_result_id) " +
                            "REFERENCES tender_result(id) ON DELETE CASCADE"))
    private TenderResult tenderResult;

    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tendering_terms_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_tenderingterms_subcontractterms",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tendering_terms_id) " +
                            "REFERENCES tendering_terms(id) ON DELETE CASCADE"))
    private TenderingTerms tenderingTerms;

    @Override
    public String toString() {

        return "SubcontractTerms: " +
                "[rate='" + rate + "', " +
                "description='" + description + "']";
    }
}
