package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidad que representa la lista corta de operadores económicos dentro de un
 * proceso de licitación ({@link TenderingProcess}).
 * <p>
 * Se utiliza para definir criterios objetivos y numéricos relacionados con la
 * selección de un número limitado de candidatos dentro de un procedimiento
 * competitivo.
 * </p>
 *
 * <p>
 * Hereda de {@link Auditable}, por lo que incluye campos de auditoría
 * (fecha de creación, última modificación, usuario, etc.).
 * </p>
 *
 * <p>
 * Cada instancia se corresponde con un registro en la tabla
 * <b>economic_operator_short_list</b> de la base de datos.
 * </p>
 *
 * @author Juan
 * @version 1.0
 * @since 20/03/2025
 */
@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "economic_operator_short_list")
public class EconomicOperatorShortList extends Auditable {

    /**
     * Identificador único de la lista corta en formato UUID.
     * Se genera automáticamente al persistir la entidad en la base de datos.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Descripción textual de los criterios objetivos para la selección
     * de un número limitado de candidatos.
     * <p>
     * Se almacena como un campo de texto largo (tipo {@code TEXT} en la base de datos).
     * </p>
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Número previsto de operadores económicos para la creación
     * de la lista corta de candidatos.
     */
    @Column(name = "expected_quantity")
    private Double expectedQuantity;

    /**
     * Número máximo de empresarios u operadores económicos que
     * pueden ser seleccionados para la lista corta de candidatos.
     */
    @Column(name = "maximum_quantity")
    private Double maximumQuantity;

    /**
     * Número mínimo de empresarios u operadores económicos que
     * deben ser seleccionados para la lista corta de candidatos.
     */
    @Column(name = "minimum_quantity")
    private Double minimumQuantity;

    /**
     * Relación uno a uno con el proceso de licitación
     * ({@link TenderingProcess}) al que pertenece esta lista corta.
     * <p>
     * La relación está configurada con eliminación en cascada
     * para asegurar la integridad referencial.
     * </p>
     */
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tendering_process_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_economicoperatorshortlist_tenderingprocess",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tendering_process_id) " +
                                    "REFERENCES tendering_process(id) ON DELETE CASCADE"))
    private TenderingProcess tenderingProcess;

    /**
     * Devuelve una representación en cadena de la entidad,
     * mostrando los valores principales de sus atributos.
     *
     * @return cadena con los valores de {@code description},
     * {@code expectedQuantity}, {@code maximumQuantity} y {@code minimumQuantity}.
     */
    @Override
    public String toString() {
        return "EconomicOperatorShortList: " +
                "[description='" + description + "', " +
                "expectedQuantity='" + expectedQuantity + "', " +
                "maximumQuantity='" + maximumQuantity + "', " +
                "minimumQuantity='" + minimumQuantity + "']";
    }
}
