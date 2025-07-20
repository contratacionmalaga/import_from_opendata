package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.common.util.Constantes;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
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
@Entity
@Table(
        name = "preliminary_market_consultation_status"
)

public class PreliminaryMarketConsultationStatus extends Auditable {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Estado de la consulta 6.1
     */
    @Column(name = "preliminary_market_consultation_id", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String preliminaryMarketConsultationID;

    /**
     * Número de expediente de la consulta 6.2
     */
    @Column(name = "preliminary_market_consultation_status_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String preliminaryMarketConsultationStatusCode;

    /**
     * Objeto de la consulta 6.3
     */
    @Column(name = "consultation_name", nullable = false, columnDefinition = "TEXT")
    private String consultationName;

    /**
     * Tipo de la consulta 6.4
     */
    @Column(name = "condition_type_code", nullable = false, columnDefinition = "TEXT")
    private String conditionTypeCode;

    /**
     * Condiciones o términos de envío de la consulta 6.6
     */
    @Column(name = "conditions_text", columnDefinition = "TEXT")
    private String conditionsText;

    /**
     * Participantes de la consulta 6.7
     */
    @Column(name = "party_selection_reason_text", columnDefinition = "TEXT")
    private String partySelectionReasonText;

    /**
     * Motivo de la selección de participante de la consulta 6.8
     */
    @Column(name = "condition_type_reason_text", columnDefinition = "TEXT")
    private String conditionTypeReasonText;

    /**
     * Fecha de inicio de la consulta 6.9
     */
    @Column(name = "planned_date")
    private LocalDate plannedDate;

    /**
     * Fecha límite de respueta de la  6.10
     */
    @Column(name = "limit_date")
    private LocalDate limitDate;

    //
    //
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "entry_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_preliminarymarketconsultationstatus_entry",
                    foreignKeyDefinition = "FOREIGN KEY (entry_id) REFERENCES entry(id) ON DELETE CASCADE"))
    private Entry entry;

    //
    //
    //

    @OneToOne(mappedBy = "preliminaryMarketConsultationStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private Attachment attachment;

    @OneToOne(mappedBy = "preliminaryMarketConsultationStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private LocatedContractingParty locatedContractingParty;

    @OneToMany(mappedBy = "preliminaryMarketConsultationStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GeneralDocument> listGeneralDocument = new ArrayList<>();

    @OneToMany(mappedBy = "preliminaryMarketConsultationStatus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NoticeInfo> listNoticeInfo = new ArrayList<>();

    @OneToOne(mappedBy = "preliminaryMarketConsultationStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProcurementProject procurementProject;

    @OneToOne(mappedBy = "preliminaryMarketConsultationStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private TenderingProcess tenderingProcess;

    @Override
    public String toString() {

        return "PreliminaryMarketConsultationStatus: [" +
                "preliminaryMarketConsultationID='" + preliminaryMarketConsultationID + "', " +
                "preliminaryMarketConsultationStatusCode='" + preliminaryMarketConsultationStatusCode +
                "preliminaryMarketConsultationStatusCode='" + preliminaryMarketConsultationStatusCode +
                "preliminaryMarketConsultationStatusCode='" + preliminaryMarketConsultationStatusCode +
                "preliminaryMarketConsultationStatusCode='" + preliminaryMarketConsultationStatusCode +
                "preliminaryMarketConsultationStatusCode='" + preliminaryMarketConsultationStatusCode +
                "preliminaryMarketConsultationStatusCode='" + preliminaryMarketConsultationStatusCode +
                "']";
    }
}
