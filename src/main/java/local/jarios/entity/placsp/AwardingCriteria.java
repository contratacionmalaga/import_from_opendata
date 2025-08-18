package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Representa un criterio de adjudicación en un proceso de licitación.
 * <p>
 * Este criterio especifica las condiciones necesarias para adjudicar un contrato,
 * pudiendo aplicarse tanto a nivel general como a nivel de lote.
 * </p>
 *
 * <p>Contiene códigos que tipifican el criterio y subtipo, una descripción, notas
 * y un valor numérico que indica la importancia o peso del criterio.</p>
 *
 * <p>Relacionada con la entidad {@link AwardingTerms} que agrupa varios criterios.</p>
 *
 * <p><b>Author:</b> Juan Antonio</p>
 * <p><b>Date:</b> 04/06/2024</p>
 * <p><b>Team:</b> Juan Antonio</p>
 */
@Setter
@Getter
@Entity
@Table(name = "awarding_criteria")
public class AwardingCriteria extends Auditable {

    /**
     * Constructor por defecto.
     * <p>
     * Requerido por JPA para la correcta creación de proxies
     * y por Lombok para la inicialización básica.
     * </p>
     */
    public AwardingCriteria() {
        // Constructor vacío requerido por JPA
    }

    /**
     * Identificador único universal (UUID) del criterio de adjudicación.
     * <p>
     * Clave primaria generada automáticamente.
     * No puede ser actualizada ni ser nula.
     * </p>
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Código que tipifica el tipo de criterio de adjudicación.
     */
    @Column(name = "awarding_criteria_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String awardingCriteriaTypeCode;

    /**
     * Código que identifica el subtipo del criterio de adjudicación.
     */
    @Column(name = "awarding_criteria_subtype_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String awardingCriteriaSubTypeCode;

    /**
     * Descripción textual del criterio de adjudicación.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Notas o detalles adicionales relativos al criterio de adjudicación.
     */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /**
     * Valor numérico que representa el peso o importancia asignada al cumplimiento
     * de este criterio dentro del proceso de licitación.
     */
    @Column(name = "weight_numeric")
    private Double weightNumeric;

    /**
     * Referencia a los términos de adjudicación a los que pertenece este criterio.
     * <p>
     * Relación muchos a uno con {@link AwardingTerms}.
     * La eliminación en cascada está definida en la clave foránea.
     * </p>
     */
    @ManyToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "awarding_terms_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_awardingcriteria_awardingterms",
                    foreignKeyDefinition = "FOREIGN KEY (awarding_terms_id) REFERENCES awarding_terms(id) ON DELETE CASCADE"))
    private AwardingTerms awardingTerms;

    /**
     * Devuelve una representación en cadena del objeto {@code AwardingCriteria}.
     *
     * @return Cadena con valores de los campos principales del criterio.
     */
    @Override
    public String toString() {
        return "AwardingCriteria: " +
                "[awardingCriteriaTypeCode='" + awardingCriteriaTypeCode + "', " +
                "awardingCriteriaSubTypeCode='" + awardingCriteriaSubTypeCode + "', " +
                "description='" + description + "', " +
                "note='" + note + "', " +
                "weightNumeric='" + weightNumeric + "']";
    }
}
