package local.jarios.entity.placsp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.Auditable;
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
    name = "party_identification"
)

public class PartyIdentification extends Auditable {

  //
  //
  //
  @Id
  @GeneratedValue(generator = "UUID")
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
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "party_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_partyidentification_party",
          foreignKeyDefinition =
              "FOREIGN KEY (party_id) " +
                  "REFERENCES party(id) ON DELETE CASCADE"))
  private Party party;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "agent_party_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_partyidentification_agentparty",
          foreignKeyDefinition =
              "FOREIGN KEY (agent_party_id) " +
                  "REFERENCES agent_party(id) ON DELETE CASCADE"))
  private AgentParty agentParty;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "winning_party_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_partyidentification_winningparty",
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
}
