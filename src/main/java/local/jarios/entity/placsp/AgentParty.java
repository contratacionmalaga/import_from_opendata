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
import lombok.Setter;

import java.util.UUID;

/**
 * Representa una entidad agente dentro del sistema, vinculada a una {@link Party}.
 * <p>
 * Contiene información relevante como la URI del sitio web y el nombre de la entidad.
 * </p>
 * <p>
 * Esta clase hereda las propiedades de auditoría de {@link Auditable}.
 * </p>
 * <p>
 * Mantiene una relación uno a uno con {@link Party} y con {@link PartyIdentification}.
 * </p>
 *
 * <p><b>Author:</b> Juan Antonio</p>
 * <p><b>Date:</b> 06/07/2024</p>
 * <p><b>Team:</b> Juan Antonio</p>
 */
@Setter
@Getter
@Entity
@Table(name = "agent_party")
public class AgentParty extends Auditable {

  /**
   * Identificador único universal (UUID) de la entidad agente.
   * <p>
   * Clave primaria generada automáticamente.
   * No puede ser actualizada ni ser nula.
   * </p>
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  /**
   * URI del sitio web asociado al agente.
   * <p>
   * Longitud máxima definida por {@link Constantes#TAMANO_MAXIMO_CAMPO_500}.
   * </p>
   */
  @Column(name = "web_site_uri", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String webSiteUri;
  /**
   * Nombre de la entidad agente.
   * <p>
   * Longitud máxima definida por {@link Constantes#TAMANO_MAXIMO_CAMPO_500}.
   * </p>
   */
  @Column(name = "party_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String partyName;
  /**
   * Entidad {@link Party} asociada a este agente.
   * <p>
   * Relación uno a uno, carga perezosa, con cascada para todas las operaciones.
   * La eliminación en cascada se asegura mediante la clave foránea.
   * </p>
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "party_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_agentparty_party",
          foreignKeyDefinition = "FOREIGN KEY (party_id) REFERENCES party(id) ON DELETE CASCADE"))
  private Party party;
  /**
   * Entidad {@link PartyIdentification} dependiente de este agente.
   * <p>
   * Relación uno a uno mapeada por el atributo {@code agentParty} en {@link PartyIdentification}.
   * Se aplican cascada completa y eliminación de huérfanos.
   * </p>
   */
  @OneToOne(mappedBy = "agentParty", cascade = CascadeType.ALL, orphanRemoval = true)
  private PartyIdentification partyIdentification;

  /**
   * Constructor por defecto.
   * <p>
   * Requerido por JPA para la correcta creación de proxies
   * y por Lombok para la inicialización básica.
   * </p>
   */
  public AgentParty() {
    // Constructor vacío requerido por JPA
  }

  /**
   * Representación textual del agente.
   * <p>
   * Incluye la URI del sitio web y el nombre del agente.
   * </p>
   *
   * @return Cadena representativa con los datos principales del agente.
   */
  @Override
  public String toString() {
    return "AgentParty: " +
        "[webSiteUri='" + webSiteUri + "', " +
        "partyName='" + partyName + "']";
  }
}
