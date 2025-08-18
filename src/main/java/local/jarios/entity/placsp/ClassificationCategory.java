package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Representa una categoría de clasificación utilizada para definir requisitos de participación
 * en un proceso de licitación.
 * <p>
 * Esta entidad corresponde a capacidades requeridas a los licitadores, que pueden aplicarse
 * tanto a nivel de lote como para toda la licitación.
 * </p>
 *
 * <p>
 * Cada categoría está asociada a un esquema de clasificación específico, definido en
 * {@link ClassificationScheme}.
 * </p>
 *
 * <p><b>Author:</b> Juan Antonio</p>
 * <p><b>Date:</b> 04/06/2024</p>
 * <p><b>Team:</b> Juan Antonio</p>
 */
@Setter
@Getter
@Entity
@Table(name = "classification_category")
public class ClassificationCategory extends Auditable {

    /**
     * Constructor por defecto.
     * <p>
     * Requerido por JPA para la correcta creación de proxies
     * y por Lombok para la inicialización básica.
     * </p>
     */
    public ClassificationCategory() {
        // Constructor vacío requerido por JPA
    }

    /**
     * Identificador único universal (UUID) de la categoría de clasificación.
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
     * Código que identifica el valor de la categoría de clasificación.
     */
    @Column(name = "code_value", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String codeValue;

    /**
     * Esquema de clasificación al que pertenece esta categoría.
     * <p>
     * Relación muchos a uno con la entidad {@link ClassificationScheme}.
     * La eliminación en cascada está configurada en la clave foránea.
     * </p>
     */
    @ManyToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "classification_scheme_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_classificationcategory_classificationscheme",
                    foreignKeyDefinition =
                            "FOREIGN KEY (classification_scheme_id) " +
                            "REFERENCES classification_scheme(id) ON DELETE CASCADE"))
    private ClassificationScheme classificationScheme;

    @Override
    public String toString() {
        return "ClassificationCategory: " +
                "[codeValue='" + codeValue + "']";
    }
}
