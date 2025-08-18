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
        name = "general_document"
)

public class GeneralDocument extends Auditable {

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
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_folder_status_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_generaldocument_contractfolderstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_folder_status_id) " +
                            "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
    private ContractFolderStatus contractFolderStatus;

    @ManyToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "preliminary_market_consultation_status_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_generaldocument_preliminarymarketconsultationstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (preliminary_market_consultation_status_id) " +
                                    "REFERENCES preliminary_market_consultation_status(id) ON DELETE CASCADE"))
    private PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus;

    //
    //
    //
    @OneToOne(mappedBy = "generalDocument", cascade = CascadeType.MERGE, orphanRemoval = true)
    private GeneralDocumentDocumentReference generalDocumentDocumentReference;

    @Override
    public String toString() {

        return "GeneralDocument: []";
    }
}
