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
        name = "tenderer_requirement"
)

// 4.30 Requisitos de participación
// Condiciones de admisión (pueden ser más de una): Requerimientos específicos que deben cumplir los
// licitadores como por ejemplo los criterios de admisión y exclusión
public class TendererRequirement extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Condiciones de admisión (pueden ser más de una): Requerimientos específicos que deben cumplir los licitadores
    //         como por ejemplo los criterios de admisión y exclusión
    @Column(name = "requirement_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String requirementTypeCode;

    // Descripción del objeto de la subcontratación
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenderer_qualification_request_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_tendererrequirement_tendererqualificationrequest",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tenderer_qualification_request_id) " +
                            "REFERENCES tenderer_qualification_request(id) ON DELETE CASCADE"))
    private TendererQualificationRequest tendererQualificationRequest;

    @Override
    public String toString() {

        return "TendererRequirement: " +
                "[requirementTypeCode='" + requirementTypeCode + "', " +
                "description='" + description + "']";
    }

    public TendererRequirement() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
