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
    name = "party"
)

public class Party extends AuditableCreatedAt {

  //
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "web_site_uri", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String webSiteUri;

  @Column(name = "endpoint_id", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String endpointId;

  @Column(name = "party_name", columnDefinition = "TEXT")
  private String partyName;

  // =========================================================================
  // RELACIONES PADRES
  // =========================================================================
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

  // =========================================================================
  // RELACIONES HIJAS
  // =========================================================================
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
        "endpointId='" + endpointId + "', " +
        "partyName='" + partyName + "']";
  }
}
