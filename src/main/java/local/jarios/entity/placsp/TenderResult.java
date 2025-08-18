package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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
@NoArgsConstructor
@Entity
@Table(
        name = "tender_result"
)

//
public class TenderResult extends Auditable {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // 4.35.1 Tipo de resultado
    @Column(name = "result_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String resultCode;

    // 4.35.5 Motivación - Descripción textual
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 4.35.5 Motivación - Fecha del acuerdo
    @Column(name = "award_date")
    private LocalDate awardDate;

    // 4.35.4 Número de licitadores participantes
    @Column(name = "received_tender_quantity")
    private Double receivedTenderQuantity;

    // 4.35.6 Ofertas recibidas - Precio de la oferta más baja
    @Column(name = "lower_tender_amount_quantity")
    private Double lowerTenderAmountQuantity;

    // 4.35.6 Ofertas recibidas - Precio de la oferta más alta
    @Column(name = "higher_tender_amount_quantity")
    private Double higherTenderAmountQuantity;

    // 4.35.7 Información sobre el contrato - Fecha de inicio del contrato
    @Column(name = "start_date", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private LocalDate startDate;

    // 4.35.6 Ofertas recibidas - Número de ofertas recibidas de pymes
    @Column(name = "smes_received_tender_quantity")
    private Double sMEsReceivedTenderQuantity;

    // 4.35.6 Ofertas recibidas - Número de ofertas recibidas de extranjeros comunitarios (UE)
    @Column(name = "eu_nationals_received_tender_quantity")
    private Double eUNationalsReceivedTenderQuantity;

    // 4.35.6 Ofertas recibidas - Número de ofertas recibidas de extranjeros comunitarios (no UE)
    @Column(name = "noneu_nationals_received_tender_quantity")
    private Double nonEUNationalsReceivedTenderQuantity;

    // 4.35.6 Ofertas recibidas - Es PYME el adjudicatario
    @Column(name = "sme_awarded_indicator")
    private Boolean sMEAwardedIndicator;

    // 4.35.7 Información sobre el contratista - Nacionalidad del contratista (código)
    @Column(name = "awarded_owner_nationality_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String awardedOwnerNationalityCode;

    // 4.35.6 Ofertas recibidas - Se han excluído ofertas por ser anormalmente bajas
    @Column(name = "abnormally_low_tenders_indicator")
    private Boolean abnormallyLowTendersIndicator;

    @ManyToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_folder_status_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_tenderresult_contractfolderstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_folder_status_id) " +
                            "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
    private ContractFolderStatus contractFolderStatus;

    //
    // OTRAS RELACIONES
    //

    // Datos del Contrato
    @OneToOne(mappedBy = "tenderResult", cascade = CascadeType.MERGE, orphanRemoval = true)
    private Contract contract;

    // Datos del Adjudicatario del de este TenderResult
    @OneToOne(mappedBy = "tenderResult", cascade = CascadeType.MERGE, orphanRemoval = true)
    private WinningParty winningParty;

    // Importe de adjudicación de este TenderResult
    @OneToOne(mappedBy = "tenderResult", cascade = CascadeType.MERGE, orphanRemoval = true)
    private TenderedProject awardedTenderedProject;

    // Información de la subcontratación de este TenderResult
    @OneToMany(mappedBy = "tenderResult", cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SubcontractTerms> listSubcontractTerms = new ArrayList<>();

    @Override
    public String toString() {

        return "TenderResult: " +
                "[resultCode='" + resultCode + "', " +
                "description='" + description + "', " +
                "awardDate='" + awardDate + "', " +
                "receivedTenderQuantity='" + receivedTenderQuantity + "', " +
                "lowerTenderAmountQuantity='" + lowerTenderAmountQuantity + "', " +
                "higherTenderAmountQuantity='" + higherTenderAmountQuantity + "', " +
                "startDate='" + startDate + "', " +
                "sMEsReceivedTenderQuantity='" + sMEsReceivedTenderQuantity + "', " +
                "eUNationalsReceivedTenderQuantity='" + eUNationalsReceivedTenderQuantity + "', " +
                "nonEUNationalsReceivedTenderQuantity='" + nonEUNationalsReceivedTenderQuantity + "', " +
                "sMEAwardedIndicator='" + sMEAwardedIndicator + "', " +
                "awardedOwnerNationalityCode='" + awardedOwnerNationalityCode + "', " +
                "abnormallyLowTendersIndicator='" + abnormallyLowTendersIndicator + "']";
    }
}
