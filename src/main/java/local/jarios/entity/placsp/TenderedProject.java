package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Description:
 * Author: juan
 * Date: 20/03/2025
 * Team:
 */

//  4.35.3 Importe de adjudicación
// Importe de adjudicación
// Importe ofertado por el licitador adjudicatario del contrato sin impuestos y con impuestos
// CARDINALIDAD - 0..n. Se podrán indicar importes de adjudicación distintos para cada adjudicatario,
//     y si hubiera lotes para cada adjudicatario en cada lote adjudicado.

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "tendered_project"
)
public class TenderedProject extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    //  Indica el resultado de cada uno de los lotes en un expediente
    @Column(name = "procurement_project_lot_id", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String procurementProjectLotId;

    //
    //
    //
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tender_result_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_tenderedproject_tenderresult",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tender_result_id) " +
                            "REFERENCES tender_result(id) ON DELETE CASCADE"))
    private TenderResult tenderResult;

    //
    //
    //
    @OneToOne(mappedBy = "awardedTenderedProject", cascade = CascadeType.MERGE, orphanRemoval = true)
    private LegalMonetaryTotal legalMonetaryTotal;

    @Override
    public String toString() {

        return "TenderedProject: " +
                "[procurementProjectLotId='" + procurementProjectLotId + "']";
    }
}
