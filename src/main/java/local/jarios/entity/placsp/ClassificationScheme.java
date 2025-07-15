package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
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
        name = "classification_scheme"
)

// 4.30 Requisitos de participación
// Capacidades requeridas a los licitadores durante el proceso de licitación.
// Puede aparecer tanto a nivel de lote como para toda la licitación
public class ClassificationScheme extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "uuid", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String uuid;

    @Column(name = "name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String name;

    @Column(name = "note", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
    private String note;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    //
    //
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tenderer_qualification_request_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_classificationscheme_tenderqualificationrequest",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tenderer_qualification_request_id) " +
                            "REFERENCES tenderer_qualification_request(id) ON DELETE CASCADE"))
    private TendererQualificationRequest tendererQualificationRequest;

    //
    //
    //
    // Clasificación empresarial solicitada: Especifica las Clasificaciones requeridas para los Licitadores
    @OneToMany(mappedBy = "classificationScheme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassificationCategory> classificationCategory;

    @Override
    public String toString() {

        return "ClassificationScheme: " +
                "[uuid='" + uuid + "', " +
                "[name='" + name + "', " +
                "[note='" + note + "', " +
                "[description='" + description + "']";
    }
}
