package local.jarios.entity.codice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidad que representa las <b>condiciones especiales de ejecución de un contrato</b> dentro de
 * los {@link TenderingTerms}.
 * <p>
 * Se utilizan para indicar requisitos o condiciones adicionales que el contratista debe cumplir
 * durante la ejecución del contrato, como cláusulas sociales, medioambientales u otros compromisos
 * específicos.
 * </p>
 *
 * <p>
 * Se almacena en la tabla <b>contract_execution_requirement</b> y hereda de {@link AuditableCreatedAt},
 * incorporando trazabilidad de auditoría.
 * </p>
 *
 * <h2>Norma de referencia</h2>
 * <ul>
 *     <li><b>4.48 Condiciones especiales de ejecución del contrato</b>:
 *     establece que deben detallarse las condiciones que van más allá
 *     de las puramente técnicas o económicas, incluyendo su descripción,
 *     nombre y código.</li>
 * </ul>
 *
 * @author Juan
 * @version 1.0
 * @since 04/06/2024
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "contract_execution_requirement")
public class ContractExecutionRequirement extends AuditableCreatedAt {

  /**
   * Identificador único de la condición especial de ejecución. Generado automáticamente como UUID.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  // =========================================================================
  // PROPIEDADES DE LA ENTIDAD
  // =========================================================================
  /**
   * Descripción detallada de la condición especial de ejecución.
   * <p>
   * Campo de tipo {@code TEXT}.
   * </p>
   */
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
  /**
   * Código identificador de la condición especial de ejecución.
   * <p>
   * Longitud máxima: {@link Constantes#TAMANO_MAXIMO_CAMPO_50}.
   * </p>
   */
  @Column(name = "execution_requirement_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String executionRequirementCode;
  /**
   * Nombre de la condición especial de ejecución.
   * <p>
   * Campo de tipo {@code TEXT}.
   * </p>
   */
  @Column(name = "name", columnDefinition = "TEXT")
  private String name;
  /**
   * Relación con los términos de licitación ({@link TenderingTerms}) a los que pertenece esta
   * condición especial de ejecución.
   * <p>
   * Si se eliminan los términos de licitación, la condición también se elimina gracias a la
   * política {@code ON DELETE CASCADE}.
   * </p>
   */
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "tendering_terms_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_contractexecutionrequirement_tenderingterms",
          foreignKeyDefinition =
              "FOREIGN KEY (tendering_terms_id) " +
                  "REFERENCES tendering_terms(id) ON DELETE CASCADE"))
  private TenderingTerms tenderingTerms;

  // =========================================================================
  // RELACIONES CON ENTIDADES PADRES
  // =========================================================================

  // =========================================================================
  // MÉTODOS AUXILIARES
  // =========================================================================

  /**
   * Representación textual simplificada de la entidad.
   *
   * @return cadena con descripción, nombre y código.
   */
  @Override
  public String toString() {
    return "ContractExecutionRequirement: " +
        "[description='" + description + "', " +
        "name='" + name + "', " +
        "executionRequirementCode='" + executionRequirementCode + "']";
  }
}
