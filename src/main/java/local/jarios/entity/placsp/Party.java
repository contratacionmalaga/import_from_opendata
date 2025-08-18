package local.jarios.entity.placsp;

import jakarta.persistence.*;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@Entity
@Table(
        name = "party"
)

public class Party extends Auditable {

    //
    //
    //
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "web_site_uri", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String webSiteUri;

    @Column(name = "party_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String partyName;

    //
    //
    //
    @OneToOne(
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "located_contracting_party_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_party_locatedcontractingparty",
                    foreignKeyDefinition =
                            "FOREIGN KEY (located_contracting_party_id) " +
                            "REFERENCES located_contracting_party(id) ON DELETE CASCADE"))
    private LocatedContractingParty locatedContractingParty;

    //
    //
    //
    @OneToOne(mappedBy = "party", cascade = CascadeType.MERGE, orphanRemoval = true)
    private Address postalAddress;

    @OneToOne(mappedBy = "party", cascade = CascadeType.MERGE, orphanRemoval = true)
    private Location physicalLocation;

    @OneToOne(mappedBy = "party", cascade = CascadeType.MERGE, orphanRemoval = true)
    private Contact contact;

    @OneToOne(mappedBy = "party", cascade = CascadeType.MERGE, orphanRemoval = true)
    private AgentParty agentParty;

    @OneToOne(mappedBy = "party", cascade = CascadeType.MERGE, orphanRemoval = true)
    private PartyIdentification partyIdentification;

    @Override
    public String toString() {

        return "Party: " +
                "[webSiteUri='" + webSiteUri + "', " +
                "partyName='" + partyName + "']";
    }
}
