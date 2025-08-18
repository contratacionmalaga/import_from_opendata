package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@Entity
@Table(
        name = "technical_document_reference"
)

public class TechnicalDocumentReference extends Auditable {

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
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_folder_status_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_technicaldocumentreference_contractfolderstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_folder_status_id) " +
                            "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
    private ContractFolderStatus contractFolderStatus;

    //
    //
    //
    @OneToOne(mappedBy = "technicalDocumentReference", cascade = CascadeType.MERGE, orphanRemoval = true)
    private DocumentReference documentReference;

    @Override
    public String toString() {

        return "TechnicalDocumentReference: []";
    }
}
