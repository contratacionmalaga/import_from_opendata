package local.jarios.entity.placsp;

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
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Description: Author: juan Date: 20/03/2025 Team:
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "location"
)
public class Location extends Auditable {

  // =========================================================================
  // PROPIEDADES DEL MODELO
  // =========================================================================
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "country_subentity", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String countrySubentity;

  @Column(name = "country_subentity_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String countrySubentityCode;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  // =========================================================================
  // RELACIONES PADRES
  // =========================================================================
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "procurement_project_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_location_procurementproject",
          foreignKeyDefinition =
              "FOREIGN KEY (procurement_project_id) " +
                  "REFERENCES procurement_project(id) ON DELETE CASCADE"))
  private ProcurementProject procurementProject;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "party_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_location_party",
          foreignKeyDefinition =
              "FOREIGN KEY (party_id) " +
                  "REFERENCES party(id) ON DELETE CASCADE"))
  private Party party;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "winning_party_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_location_winningparty",
          foreignKeyDefinition =
              "FOREIGN KEY (winning_party_id) " +
                  "REFERENCES winning_party(id) ON DELETE CASCADE"))
  private WinningParty winningParty;

  // =========================================================================
  // RELACIONES HIJAS
  // =========================================================================
  @OneToOne(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
  private Address address;

  // =========================================================================
  // OTROS MÉTODOS
  // =========================================================================
  /**
   * Devuelve una representación en cadena del objeto con los valores principales de la entidad.
   *
   * @return cadena con los valores de {@code countrySubentity} y {@code countrySubentityCode}.
   */
  @Override
  public String toString() {

    return "Location: " +
        "[countrySubentity='" + countrySubentity + "', " +
        "countrySubentityCode='" + countrySubentityCode + "']";
  }
}


