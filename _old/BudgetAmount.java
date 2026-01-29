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
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Representa las cantidades presupuestarias asociadas a un proyecto de contratación.
 * <p>
 * Esta entidad almacena diferentes importes relacionados con el presupuesto, incluyendo el importe
 * estimado global del contrato, el importe total y el importe excluyendo impuestos.
 * </p>
 *
 * <p>Está vinculada a un proyecto de contratación específico mediante una relación uno a uno
 * con la entidad {@link ProcurementProject}.</p>
 *
 * <p><b>Author:</b> juan</p>
 * <p><b>Date:</b> 20/03/2025</p>
 * <p><b>Team:</b> (sin especificar)</p>
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "budget_amount")
public class BudgetAmount extends AuditableCreatedAt {

  /**
   * Identificador único universal (UUID) de la cantidad presupuestaria.
   * <p>
   * Clave primaria generada automáticamente. No puede ser actualizada ni ser nula.
   * </p>
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;
  /**
   * Importe estimado global del contrato.
   */
  @Column(name = "estimated_overall_contract_amount")
  private Double estimatedOverallContractAmount;
  /**
   * Importe total presupuestado.
   */
  @Column(name = "total_amount")
  private Double totalAmount;
  /**
   * Importe presupuestado excluyendo impuestos.
   */
  @Column(name = "tax_exclusive_amount")
  private Double taxExclusiveAmount;
  /**
   * Proyecto de contratación al que está asociado este presupuesto.
   * <p>
   * Relación uno a uno con la entidad {@link ProcurementProject}. La eliminación en cascada está
   * configurada en la clave foránea.
   * </p>
   */
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "procurement_project_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_budgetamount_procurementproject",
          foreignKeyDefinition = "FOREIGN KEY (procurement_project_id) REFERENCES procurement_project(id) ON DELETE CASCADE"))
  private ProcurementProject procurementProject;

  @Override
  public String toString() {
    return "BudgetAmount: " +
        "[estimatedOverallContractAmount='" + estimatedOverallContractAmount + "', " +
        "totalAmount='" + totalAmount + "', " +
        "taxExclusiveAmount='" + taxExclusiveAmount + "']";
  }
}
