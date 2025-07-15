package local.jarios.entity.placsp;

import jakarta.persistence.*;
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
        name = "classification_category"
)

// 4.30 Requisitos de participación
// Capacidades requeridas a los licitadores durante el proceso de licitación.
// Puede aparecer tanto a nivel de lote como para toda la licitación
public class ClassificationCategory extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Código
    @Column(name = "code_value", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String codeValue;

    //
    //
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "classification_scheme_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_classificationscheme_classificationscheme",
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
