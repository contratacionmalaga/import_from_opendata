package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
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
        name = "party"
)

public class Party extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "web_site_uri", length = Constantes.TAMANO_MAXIMO_CAMPO_450)
    private String webSiteUri;

    @Column(name = "party_name", length = Constantes.TAMANO_MAXIMO_CAMPO_450)
    private String partyName;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "located_contracting_party_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_locatedcontractingparty_party",
                    foreignKeyDefinition =
                            "FOREIGN KEY (located_contracting_party_id) " +
                            "REFERENCES located_contracting_party(id) ON DELETE CASCADE"))
    private LocatedContractingParty locatedContractingParty;

    //
    //
    //
    @OneToOne(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private Address postalAddress;

    @OneToOne(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private Location physicalLocation;

    @OneToOne(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private Contact contact;

    @OneToOne(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private AgentParty agentParty;

    @OneToOne(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private PartyIdentification partyIdentification;

    @Override
    public String toString() {

        return "Party: " +
                "[webSiteUri='" + webSiteUri + "', " +
                "partyName='" + partyName + "']";
    }

    //
    //
    //
    public Party() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
