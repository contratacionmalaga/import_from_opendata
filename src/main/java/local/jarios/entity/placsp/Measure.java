package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad JPA que representa una medida de duración asociada a un {@link Period}.
 * <p>
 * Esta clase modela una duración con una unidad y un valor numérico, que puede ser
 * parte de estructuras más complejas en PLACSP u otros modelos de contratación pública.
 * </p>
 *
 * <p>La entidad hereda campos de auditoría desde {@link Auditable}.</p>
 *
 * @author Juan
 * @since 2024-06
 */
@Getter
@Setter
@Entity
@Table(name = "meassure")
public class Measure extends Auditable {

    /**
     * Identificador único de la entidad. Se genera automáticamente usando UUID versión 7.
     */
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Código de la unidad de duración (por ejemplo, "DAY", "MONTH", "YEAR").
     */
    @Column(name = "unit_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String unitCode;

    /**
     * Valor numérico de la duración expresada en la unidad indicada.
     */
    @Column(name = "value")
    private BigDecimal value;

    /**
     * Relación uno a uno con la entidad {@link Period}.
     * <p>Define la duración dentro de un período específico.</p>
     */
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "period_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_period_measure",
                    foreignKeyDefinition = "FOREIGN KEY (period_id) REFERENCES period(id) ON DELETE CASCADE")
    )
    private Period period;

    /**
     * Relación uno a uno con la entidad {@link Period}.
     * <p>Define la duración dentro de un período específico.</p>
     */
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_modification_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_contract_modification_measure",
                    foreignKeyDefinition = "FOREIGN KEY (contract_modification_id) " +
                            "REFERENCES contract_modification(id) ON DELETE CASCADE")
    )
    private ContractModification contractModification;

    /**
     * Constructor por defecto. Genera automáticamente un UUID basado en el tiempo (versión 7).
     */
    public Measure() {
        this.id = Generators.timeBasedEpochGenerator().generate();
    }

    /**
     * Devuelve una representación en texto del objeto, útil para depuración.
     *
     * @return Cadena con los valores de unitCode y value.
     */
    @Override
    public String toString() {

        return "Measure: " +
                "[unitCode='" + unitCode + "', " +
                "value='" + value + "']";
    }
}
