package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Description:
 * Author: juan
 * Date: 20/03/2025
 * Team:
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "period"
)
public class Period extends Auditable {

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "procurement_project_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_period_procurementproject",
                    foreignKeyDefinition =
                            "FOREIGN KEY (procurement_project_id) " +
                            "REFERENCES procurement_project(id) ON DELETE CASCADE"))
    private ProcurementProject procurementProject;

    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tendering_process_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_period_tenderingprocess",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tendering_process_id) " +
                            "REFERENCES tendering_process(id) ON DELETE CASCADE"))
    private TenderingProcess tenderingProcess;

    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_extension_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_period_contractextension",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_extension_id) " +
                            "REFERENCES contract_extension(id) ON DELETE CASCADE"))
    private ContractExtension contractExtension;

    @OneToOne(mappedBy = "period", cascade = CascadeType.MERGE, orphanRemoval = true)
    private Measure durationMeasure;

    @Override
    public String toString() {

        return "Period: " +
                "[startDateTime='" + startDateTime + "', " +
                "endDateTime='" + endDateTime + "', " +
                "description='" + description + "']";
    }
}
