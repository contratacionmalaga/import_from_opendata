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
        name = "located_contracting_party"
)

public class LocatedContractingParty extends Auditable {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "contracting_party_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String contractingPartyTypeCode;

    @Column(name = "buyer_profile_uri_id", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String buyerProfileUriId;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_folder_status_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_lcp_contractfolderstatus",
                    foreignKeyDefinition =
                            "FOREIGN KEY (contract_folder_status_id) " +
                            "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
    private ContractFolderStatus contractFolderStatus;

    //
    //
    // Datos de la entidad que licita
    @OneToOne(mappedBy = "locatedContractingParty", cascade = CascadeType.ALL, orphanRemoval = true)
    private Party party;

    @Override
    public String toString() {

        return "LocatedContractingParty: " +
                "[contractingPartyTypeCode='" + contractingPartyTypeCode + "', " +
                "buyerProfileUriId='" + buyerProfileUriId + "']";
    }
}
