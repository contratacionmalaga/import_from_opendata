package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Description:
 * Author: juan
 * Date: 20/03/2025
 * Team:
 */


@Setter
@Getter
@Entity
@Table(
        name = "contract_extension"
)
public class ContractExtension extends Auditable {

    //
    // PROPIEDADES DE LA ENTIDAD
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Opciones que se deben ejercer a la adjudicación del contrato
    @Column (name = "options_description", columnDefinition = "TEXT")
    private String  optionsDescription;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "procurement_project_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_procurementproject_contractextension",
                    foreignKeyDefinition =
                            "FOREIGN KEY (procurement_project_id) " +
                            "REFERENCES procurement_project(id) ON DELETE CASCADE"))
    private ProcurementProject procurementProject;

    //
    // RELACIONES CON ENTIDADES HIJAS
    //

    // Descripción del periodo de validez en el que el órgano de contratación puede ejercitar el derecho
    @OneToOne(mappedBy = "contractExtension", cascade = CascadeType.ALL, orphanRemoval = true)
    private Period optionValidityPeriod;

    @Override
    public String toString() {

        return "ContractExtension: " +
                "[optionsDescription='" + optionsDescription + "']";
    }
    //
    // CONSTRUCTOR DE LA CLASE EN EL QUE SE GENERA EL UUID VERSIÓN 7
    //
    public ContractExtension() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
