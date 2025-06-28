package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
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
        name = "party_identification"
)

public class PartyIdentification extends Auditable {

    //
    //
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "id_plataforma", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String idPlataforma;

    @Column(name = "dir3", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String dir3;

    @Column(name = "nif", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String nif;

    @Column(name = "id_oc_plat", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String idOcPlat;

    @Column(name = "otros", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String otros;

    //
    //
    //
    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "party_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_party_partyidentification",
                    foreignKeyDefinition =
                            "FOREIGN KEY (party_id) " +
                            "REFERENCES party(id) ON DELETE CASCADE"))
    private Party party;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_party_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_agentparty_partyidentification",
                    foreignKeyDefinition =
                            "FOREIGN KEY (agent_party_id) " +
                            "REFERENCES agent_party(id) ON DELETE CASCADE"))
    private AgentParty agentParty;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "winning_party_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_winningparty_partyidentification",
                    foreignKeyDefinition =
                            "FOREIGN KEY (winning_party_id) " +
                            "REFERENCES winning_party(id) ON DELETE CASCADE"))
    private WinningParty winningParty;

    @Override
    public String toString() {

        return "PartyIdentification: " +
                "[idPlataforma='" + idPlataforma + "', " +
                "dir3='" + dir3 + "', " +
                "nif='" + nif + "', " +
                "idOcPlat='" + idOcPlat + "', " +
                "otros='" + otros + "']";
    }

    //
    //
    //
    public PartyIdentification() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
