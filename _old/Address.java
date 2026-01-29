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
 * Representa una dirección física asociada a diferentes entidades del sistema.
 * <p>
 * Contiene información detallada sobre la línea de dirección, ciudad, código postal y subdivisiones
 * administrativas del país.
 * </p>
 * <p>
 * Mantiene relaciones con las entidades {@link Party}, {@link Location}, {@link WinningParty} como
 * entidades padres, y con {@link Country} como entidad hija.
 * </p>
 * <p>
 * Hereda propiedades de auditoría de {@link AuditableCreatedAt}.
 * </p>
 *
 * @author juan
 * @since 20/03/2025
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "address")
public class Address extends AuditableCreatedAt {

  /**
   * Identificador único universal (UUID) de la dirección.
   * <p>
   * Clave primaria generada automáticamente. No puede ser actualizada ni ser nula.
   * </p>
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  /**
   * Línea de dirección detallada.
   * <p>
   * Campo obligatorio (no nulo) de tipo texto.
   * </p>
   */
  @Column(name = "address_line", nullable = false, columnDefinition = "TEXT")
  private String addressLine;
  /**
   * Nombre de la ciudad correspondiente a la dirección.
   * <p>
   * Longitud máxima definida por {@link Constantes#TAMANO_MAXIMO_CAMPO_500}.
   * </p>
   */
  @Column(name = "city_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String cityName;
  /**
   * Código postal o zona postal de la dirección.
   * <p>
   * Longitud máxima definida por {@link Constantes#TAMANO_MAXIMO_CAMPO_500}.
   * </p>
   */
  @Column(name = "postal_zone", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String postalZone;
  /**
   * Código de la subdivisión administrativa del país (ej. estado, provincia).
   * <p>
   * Longitud máxima definida por {@link Constantes#TAMANO_MAXIMO_CAMPO_500}.
   * </p>
   */
  @Column(name = "country_subentity_code", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String countrySubentityCode;
  /**
   * Nombre de la subdivisión administrativa del país (ej. estado, provincia).
   * <p>
   * Longitud máxima definida por {@link Constantes#TAMANO_MAXIMO_CAMPO_500}.
   * </p>
   */
  @Column(name = "country_subentity", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String countrySubentity;

  // =========================================================================
  // RELACIONES PADRES
  // =========================================================================
  /**
   * Entidad {@link Party} asociada a esta dirección.
   * <p>
   * Relación uno a uno, carga perezosa, La eliminación en cascada se asegura mediante la clave
   * foránea en la base de datos (ON DELETE CASCADE).
   * </p>
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "party_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_address_party",
          foreignKeyDefinition = "FOREIGN KEY (party_id) REFERENCES party(id) ON DELETE CASCADE"))
  private Party party;
  /**
   * Entidad {@link Location} asociada a esta dirección.
   * <p>
   * Relación uno a uno, carga perezosa, La eliminación en cascada se asegura mediante la clave
   * foránea en la base de datos (ON DELETE CASCADE).
   * </p>
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "location_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_address_location",
          foreignKeyDefinition = "FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE CASCADE"))
  private Location location;
  /**
   * Entidad {@link WinningParty} asociada a esta dirección.
   * <p>
   * Relación uno a uno, carga perezosa, La eliminación en cascada se asegura mediante la clave
   * foránea en la base de datos (ON DELETE CASCADE).
   * </p>
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "winningparty_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_address_winningparty",
          foreignKeyDefinition = "FOREIGN KEY (winningparty_id) REFERENCES winning_party(id) ON DELETE CASCADE"))
  private WinningParty winningParty;


  // =========================================================================
  // RELACIONES HIJAS
  // =========================================================================
  /**
   * Entidad {@link Country} que depende de esta dirección.
   * <p>Relación uno a uno mapeada por el atributo {@code address} en {@link Country}. Se aplican
   * cascada completa y eliminación de huérfanos.</p>
   */
  @OneToOne(mappedBy = "address", cascade = CascadeType.ALL, orphanRemoval = true)
  private Country country;

  // =========================================================================
  // OTROS MÉTODOS
  // =========================================================================
  /**
   * Representación textual de la dirección.
   * <p>
   * Incluye línea de dirección, ciudad, código postal y subdivisiones administrativas.
   * </p>
   *
   * @return Cadena representativa con los datos principales de la dirección.
   */
  @Override
  public String toString() {
    return "Address: " +
        "[addressLine='" + addressLine + "', " +
        "cityName='" + cityName + "', " +
        "postalZone='" + postalZone + "', " +
        "countrySubentityCode='" + countrySubentityCode + "', " +
        "countrySubentity='" + countrySubentity + "']";
  }
}
