package local.jarios.entity.codice;

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
 * Entidad que representa un país dentro del sistema.
 * <p>
 * La clase modela los datos básicos de un país (código de identificación y nombre), así como su
 * relación con una dirección asociada.
 * </p>
 *
 * <p>
 * Hereda de {@link AuditableCreatedAt}, lo que permite registrar información de auditoría como fecha de
 * creación, última modificación y usuario responsable.
 * </p>
 *
 * <p>
 * Cada instancia de esta entidad se corresponde con un registro en la tabla
 * <b>country</b> de la base de datos.
 * </p>
 *
 * @author Juan
 * @version 1.0
 * @since 20/03/2025
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "country")
public class Country extends AuditableCreatedAt {

  /**
   * Identificador único de la entidad en formato UUID. Se genera automáticamente al persistir la
   * entidad en la base de datos.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  /**
   * Código de identificación único del país.
   * <p>
   * Por ejemplo, puede corresponderse con un código ISO alfa-2 o alfa-3.
   * </p>
   */
  @Column(name = "identification_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String identificationCode;
  /**
   * Nombre completo del país.
   * <p>
   * Se almacena como texto con un máximo de 500 caracteres.
   * </p>
   */
  @Column(name = "name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String name;
  /**
   * Relación uno a uno con la entidad {@link Address}.
   * <p>
   * Permite asociar el país a una dirección concreta. Esta relación está definida con eliminación
   * en cascada para mantener la integridad referencial.
   * </p>
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "address_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_country_address",
          foreignKeyDefinition = "FOREIGN KEY (address_id) REFERENCES address(id) ON DELETE CASCADE"))
  private Address address;


  // =========================================================================
  // OTROS MÉTODOS
  // =========================================================================
  /**
   * Devuelve una representación en cadena del objeto con los valores principales de la entidad.
   *
   * @return cadena con los valores de {@code identificationCode} y {@code name}.
   */
  @Override
  public String toString() {
    return "Country: " +
        "[identificationCode='" + identificationCode + "', " +
        "name='" + name + "']";
  }
}
