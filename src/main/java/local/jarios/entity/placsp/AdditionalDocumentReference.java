package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
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
        name = "additional_document_reference"
)
public class AdditionalDocumentReference extends Auditable {

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
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_folder_status_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_aditionaldocumentreference_contractfolderstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_folder_status_id) " +
                            "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
    private ContractFolderStatus contractFolderStatus;

    //
    //
    //
    @OneToOne(mappedBy = "additionalDocumentReference", cascade = CascadeType.ALL, orphanRemoval = true)
    private DocumentReference documentReference;

}
