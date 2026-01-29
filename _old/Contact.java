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
 * Entidad que representa un <b>Contacto</b> asociado a una Parte o a una Parte Adjudicataria dentro
 * del sistema PLACSP. Contiene la información de comunicación básica: nombre, teléfono, fax y
 * correo electrónico. Se almacena en la tabla <b>contact</b>.
 *
 * @author Juan
 * @since 20/03/2025
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "contact")
public class Contact extends AuditableCreatedAt {

  // =========================================================================
  // PROPIEDADES DE LA ENTIDAD
  // =========================================================================
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  @Column(name = "name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String name;
  @Column(name = "telephone", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String telephone;
  @Column(name = "telefax", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String telefax;
  @Column(name = "electronic_mail", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String electronicMail;

  // =========================================================================
  // RELACIONES PADRES
  // =========================================================================
  /**
   * Relación con la Parte genérica que posee este contacto.
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "party_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_contact_party",
          foreignKeyDefinition =
              "FOREIGN KEY (party_id) " +
                  "REFERENCES party(id) ON DELETE CASCADE"))
  private Party party;

  /**
   * Relación con la Parte adjudicataria (winning_party) que posee este contacto.
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "winning_party_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_contact_winningparty",
          foreignKeyDefinition =
              "FOREIGN KEY (winning_party_id) " +
                  "REFERENCES winning_party(id) ON DELETE CASCADE"))
  private WinningParty winningParty;

  // =========================================================================
  // MÉTODOS AUXILIARES
  // =========================================================================
  /**
   * Devuelve una representación en cadena del objeto con los valores principales de la entidad.
   *
   * @return cadena con los valores de {@code name}, {@code telephone}, {@code telefax},
   *    {@code electronicMail}.
   */
  @Override
  public String toString() {
    return "Contact: " +
        "[name='" + name + "', " +
        "telephone='" + telephone + "', " +
        "telefax='" + telefax + "', " +
        "electronicMail='" + electronicMail + "']";
  }
}
