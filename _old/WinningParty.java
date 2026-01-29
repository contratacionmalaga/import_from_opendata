package local.jarios.entity.codice;

import jakarta.persistence.CascadeType;
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
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Description: Juan Antonio Author: juan Date: 06/07/2024 Team: Juan Antonio
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "winning_party"
)

public class WinningParty extends AuditableCreatedAt {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "party_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String partyName;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "tender_result_id",
      nullable = false,
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

    return "WinningParty: " +
        "[partyName='" + partyName + "']";
  }
}
