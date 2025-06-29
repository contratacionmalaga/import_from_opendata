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
        name = "winning_party"
)

public class WinningParty extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "party_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
    private String partyName;

    @OneToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tender_result_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_winningparty_tenderresult",
                    foreignKeyDefinition =
                            "FOREIGN KEY (tender_result_id) " +
                            "REFERENCES tender_result(id) ON DELETE CASCADE"))
    private TenderResult tenderResult;

    //
    //
    //
    @OneToOne(mappedBy = "winningParty", cascade = CascadeType.ALL, orphanRemoval = true)
    private Address postalAddress;

    @OneToOne(mappedBy = "winningParty", cascade = CascadeType.ALL, orphanRemoval = true)
    private Location physicalLocation;

    @OneToOne(mappedBy = "winningParty", cascade = CascadeType.ALL, orphanRemoval = true)
    private Contact contact;

    @OneToOne(mappedBy = "winningParty", cascade = CascadeType.ALL, orphanRemoval = true)
    private PartyIdentification partyIdentification;

    @Override
    public String toString() {

        return "PartyName: " +
                "[partyName='" + partyName + "']";
    }

    public WinningParty() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
