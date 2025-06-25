package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

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
@Table(name = "duration_meassure")
public class DurationMeasure extends Auditable {

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
    private Double value;

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
                    name = "fk_period_duration_meassure",
                    foreignKeyDefinition = "FOREIGN KEY (period_id) REFERENCES period(id) ON DELETE CASCADE")
    )
    private Period period;

    /**
     * Constructor por defecto. Genera automáticamente un UUID basado en el tiempo (versión 7).
     */
    public DurationMeasure() {
        this.id = Generators.timeBasedEpochGenerator().generate();
    }

    /**
     * Devuelve una representación en texto del objeto, útil para depuración.
     *
     * @return Cadena con los valores de unitCode y value.
     */
    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }
}
