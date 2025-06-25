package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
        name = "tendering_process"
)

public class TenderingProcess extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // 4.12 Tipo de procedimiento
    @Column(name = "procedure_code", length = Constantes.TAMANO_MAXIMO_CAMPO_5)
    private String procedureCode;

    // 4.13 Sistema de contratación
    @Column(name = "contracting_system_code", length = Constantes.TAMANO_MAXIMO_CAMPO_5)
    private String contractingSystemCode;

    // 4.14 Tipo de tramitación
    @Column(name = "urgency_code", length = Constantes.TAMANO_MAXIMO_CAMPO_5)
    private String urgencyCode;

    // 4.15 Forma de la presentación de la oferta
    @Column(name = "submission_method_code", length = Constantes.TAMANO_MAXIMO_CAMPO_5)
    private String submissionMethodCode;

    // 4.34 Limitación del número de licitadores - Número de lotes a los que se debe ofertar
    @Column(name = "part_presentation_code", length = Constantes.TAMANO_MAXIMO_CAMPO_5)
    private String partPresentationCode;

    // 4.34 Limitación del número de licitadores - Número de lotes a los que se puede ofertar
    @Column(name = "maximum_lot_presentation_quantity")
    private Double maximumLotPresentationQuantity;

    // 4.34 Limitación del número de licitadores - Número máximo de lotes que se puede adjudicar a un licitador
    @Column(name = "maximun_tenderer_awarded_lot_quantity")
    private Double maximunTendererAwardedLotQuantity;

    // 4.34 Limitación del número de licitadores -
    // El poder adjudicador se reserva el derecho de adjudicar contratos que combinen lotes
    @Column(name = "lots_combination_contracting_authority_rights", columnDefinition = "TEXT")
    private String lotsCombinationContractingAuthorityRights;

    // 4.44 Contrato SARA/Umbra
    // Indica si la licitación es de Regulación armonizada para la directiva 24/2014
    // o si supera el umbral comunitario en caso de tratarse de otras directivas
    @Column(name = "overthreshold_indicator")
    private Boolean overThresholdIndicator;

    // Descripción
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Fecha y Hora límite para obtener los pliegos
    @Column(name = "document_availability_period")
    private LocalDateTime documentAvailabilityPeriod;

    // Fecha y hora límites para la presentación de oferta
    @Column(name = "tender_submission_deadline_period")
    private LocalDateTime tenderSubmissionDeadlinePeriod;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_folder_status_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_tenderingprocess_contractfolderstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_folder_status_id) " +
                            "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
    private ContractFolderStatus contractFolderStatus;

    //
    //
    //

    // 4.40 Usa Subasta Electrónica
    // Permite indicar si se va a recurrir a una subasta electrónica para adjudicar el contrato
    //
    @OneToOne(mappedBy = "tenderingProcess", cascade = CascadeType.ALL, orphanRemoval = true)
    private AuctionTerms auctionTerms;

    // 4.37 Justificación del proceso
    // Justificación del uso de un determinado procedimiento no ordinario como el
    //     procedimiento negociado sin publicidad.
    @OneToMany(mappedBy = "tenderingProcess", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcessJustification> listProcessJustification;

    // 4.37 Justificación del proceso
    // Justificación del uso de un determinado procedimiento no ordinario como el
    //     procedimiento negociado sin publicidad.
    @OneToOne(mappedBy = "tenderingProcess", cascade = CascadeType.ALL, orphanRemoval = true)
    private EconomicOperatorShortList economicOperatorShortList;

    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }

    //
    //
    //
    public TenderingProcess() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }

}
