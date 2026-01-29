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
 * Description: Importaciones de Ficheros Excel desde Internet Author: Juan Antonio Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "located_contracting_party"
)

public class LocatedContractingParty extends AuditableCreatedAt {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "contracting_party_type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String contractingPartyTypeCode;

  @Column(name = "buyer_profile_uri_id", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String buyerProfileUriId;

  // =========================================================================
  // RELACIONES PADRES
  // =========================================================================
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contract_folder_status_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_locatedcontractingparty_contractfolderstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (contract_folder_status_id) " +
                  "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus contractFolderStatus;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "preliminary_market_consultation_status_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_locatedcontractingparty_preliminarymarketconsultationstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (preliminary_market_consultation_status_id) " +
                  "REFERENCES preliminary_market_consultation_status(id) ON DELETE CASCADE"))
  private PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus;

  // =========================================================================
  // RELACIONES HIJAS
  // =========================================================================
  @OneToOne(mappedBy = "locatedContractingParty", cascade = CascadeType.ALL, orphanRemoval = true)
  private Party party;

  // =========================================================================
  // MÉTODOS AUXILIARES
  // =========================================================================
  /**
   * Devuelve una representación en cadena del objeto con los valores principales de la entidad.
   *
   * @return cadena con los valores de {@code contractingPartyTypeCode}, {@code buyerProfileUriId}.
   */
  @Override
  public String toString() {
    return "LocatedContractingParty: " +
        "[contractingPartyTypeCode='" + contractingPartyTypeCode + "', " +
        "buyerProfileUriId='" + buyerProfileUriId + "']";
  }
}
