package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidad que representa las extensiones u opciones adicionales de un contrato
 * dentro de un {@link ProcurementProject}.
 * <p>
 * Las extensiones de contrato (Contract Extension) permiten modelar
 * las posibles opciones de prórroga o ampliación que pueden ejercerse
 * tras la adjudicación de un contrato. Estas opciones quedan sujetas a
 * un periodo de validez específico durante el cual el órgano de contratación
 * puede decidir activarlas.
 * </p>
 *
 * <p>
 * Se almacena en la tabla <b>contract_extension</b> y hereda de
 * {@link Auditable}, por lo que incluye trazabilidad de auditoría.
 * </p>
 *
 * @author Juan
 * @version 1.0
 * @since 20/03/2025
 */
@Setter
@Getter
@Entity
@Table(name = "contract_extension")
public class ContractExtension extends Auditable {

    /**
     * Constructor por defecto.
     * <p>
     * Requerido por JPA para la correcta creación de proxies
     * y por Lombok para la inicialización básica.
     * </p>
     */
    public ContractExtension() {
        // Constructor vacío requerido por JPA
    }

    // =========================================================================
    // PROPIEDADES DE LA ENTIDAD
    // =========================================================================

    /**
     * Identificador único del registro de extensión contractual.
     * Generado automáticamente como UUID.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Texto descriptivo de las opciones que pueden ejercerse
     * tras la adjudicación del contrato.
     * <p>
     * Se almacena como campo de tipo {@code TEXT}.
     * </p>
     */
    @Column(name = "options_description", columnDefinition = "TEXT")
    private String optionsDescription;

    // =========================================================================
    // RELACIONES CON ENTIDADES PADRES
    // =========================================================================

    /**
     * Proyecto de contratación al que pertenece esta extensión.
     * Relación uno-a-uno con {@link ProcurementProject}.
     * <p>
     * Si se elimina el {@link ProcurementProject}, la extensión también se elimina
     * debido a la política {@code ON DELETE CASCADE}.
     * </p>
     */
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "procurement_project_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_contractextension_procurementproject",
                    foreignKeyDefinition =
                            "FOREIGN KEY (procurement_project_id) " +
                                    "REFERENCES procurement_project(id) ON DELETE CASCADE"))
    private ProcurementProject procurementProject;

    // =========================================================================
    // RELACIONES CON ENTIDADES HIJAS
    // =========================================================================

    /**
     * Periodo de validez en el que el órgano de contratación
     * puede ejercitar el derecho a prórroga u opción contractual.
     * Relación uno-a-uno con {@link Period}.
     */
    @OneToOne(mappedBy = "contractExtension", cascade = CascadeType.MERGE, orphanRemoval = true)
    private Period optionValidityPeriod;

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    /**
     * Representación textual simplificada de la entidad.
     *
     * @return cadena con el campo {@code optionsDescription}.
     */
    @Override
    public String toString() {
        return "ContractExtension: " +
                "[optionsDescription='" + optionsDescription + "']";
    }
}
