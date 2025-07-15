package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Setter
@Getter
@Entity
@Table(
        name = "attachment"
)
public class Attachment extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_attachment_documentreference",
                    foreignKeyDefinition =
                            "FOREIGN KEY (document_reference_id) " +
                            "REFERENCES document_reference(id) ON DELETE CASCADE"))
    private DocumentReference documentReference;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "additional_publication_document_reference_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_attachment_additionalpublicationdocumentreference",
                    foreignKeyDefinition =
                            "FOREIGN KEY (additional_publication_document_reference_id) " +
                            "REFERENCES additional_publication_document_reference(id) ON DELETE CASCADE"))
    private AdditionalPublicationDocumentReference additionalPublicationDocumentReference;

    //
    //
    //
    @OneToOne(mappedBy = "attachment", cascade = CascadeType.ALL, orphanRemoval = true)
    private ExternalReference externalReference;
}
