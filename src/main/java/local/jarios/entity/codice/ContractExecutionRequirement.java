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
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa las <b>condiciones especiales de ejecución de un contrato</b> dentro de
 * los {@link TenderingTerms}.
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
  /** Descripción detallada de la condición especial de ejecución. */
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  /** Código identificador de la condición especial de ejecución. */
  @Column(name = "execution_requirement_code", length = TamanoCampos.TAMANO_50)
  private String executionRequirementCode;

  /** Nombre de la condición especial de ejecución. */
  @Column(name = "name", columnDefinition = "TEXT")
  private String name;

  /**
   * Relación con los términos de licitación ({@link TenderingTerms}) a los que pertenece esta
   * condición especial de ejecución.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "tendering_terms_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "contractexecutionrequirement_tenderingterms",
              foreignKeyDefinition =
                  "FOREIGN KEY (tendering_terms_id) "
                      + "REFERENCES tendering_terms(id) ON DELETE CASCADE"))
  private TenderingTerms tenderingTerms;
}
