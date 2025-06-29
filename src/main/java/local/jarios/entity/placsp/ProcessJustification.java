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
        name = "process_justification"
)
public class ProcessJustification extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Código del motivo: Código que tipifica el motivo por los que se seleccionó el
    //     procedimiento extraordinario de contratación.
    // La lista de códigos se encuentra en
    //         [...](http://contrataciondelestado.es/codice/cl/2.0/ProcessJustificationReasonCode-2.0.gc)
    @Column(name = "reason_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String reasonCode;

    // Descripción: Descripción textual de la jusitificación de la utilización de un determinado
    //     procedimiento de contratación.
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tendering_process_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_processjustification_tenderingprocess",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tendering_process_id) " +
                            "REFERENCES tendering_process(id) ON DELETE CASCADE"))
    private TenderingProcess tenderingProcess;

    @Override
    public String toString() {

        return "ProcessJustification: " +
                "[reasonCode='" + reasonCode + "', " +
                "description='" + description + "']";
    }

    public ProcessJustification() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
