package local.jarios.mappers.codice;

import local.jarios.entity.codice.BudgetAmount;
import local.jarios.entity.codice.ProcurementProject;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.BudgetAmountType;

import java.util.Optional;

/**
 * Description: Juan Antonio Author: juan Date: 06/07/2024 Team: Juan Antonio.
 */
@Slf4j
public final class MapperBudgetAmount {

  private MapperBudgetAmount() {
  }

  public static BudgetAmount getBudgetAmount(
      ProcurementProject procurementProject,
      BudgetAmountType budgetAmountType) {

    //
    if (budgetAmountType == null) {
      return null;
    }

    //
    BudgetAmount budgetAmount = new BudgetAmount();
    budgetAmount.setProcurementProject(procurementProject);

    Optional.ofNullable(budgetAmountType.getEstimatedOverallContractAmount())
        .map(amount -> amount.getValue().doubleValue())
        .ifPresent(budgetAmount::setEstimatedOverallContractAmount);

    Optional.ofNullable(budgetAmountType.getTotalAmount())
        .map(amount -> amount.getValue().doubleValue())
        .ifPresent(budgetAmount::setTotalAmount);

    Optional.ofNullable(budgetAmountType.getTaxExclusiveAmount())
        .map(amount -> amount.getValue().doubleValue())
        .ifPresent(budgetAmount::setTaxExclusiveAmount);

    return budgetAmount;
  }
}
