package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
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
        name = "additional_publication_document_reference"
)
public class AdditionalPublicationDocumentReference extends Auditable {

    //
    // PROPIEDADES DEL MODELO
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "document_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String documentTypeCode;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "additional_publication_status_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_apdr_aps",
                    foreignKeyDefinition =
                            "FOREIGN KEY (additional_publication_status_id) " +
                            "REFERENCES additional_publication_status(id) ON DELETE CASCADE"))
    private AdditionalPublicationStatus additionalPublicationStatus;

    //
    // RELACIONES CON ENTIDADES HIJAS DEPENDIENTE DE ESTA
    //
    @OneToOne(
            mappedBy = "additionalPublicationDocumentReference",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Attachment attachment;

    @Override
    public String toString() {

        return ToStringUtil.autoToString(this);
    }

    //
    // CONSTRUCTOR DE LA CLASE EN EL QUE SE GENERA EL UUID VERSIÓN 7
    //
    public AdditionalPublicationDocumentReference() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
