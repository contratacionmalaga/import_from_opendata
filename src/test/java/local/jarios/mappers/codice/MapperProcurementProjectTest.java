package local.jarios.mappers.codice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.dgpe.codice.common.caclib.BudgetAmountType;
import org.dgpe.codice.common.caclib.ProcurementProjectLotType;
import org.dgpe.codice.common.caclib.ProcurementProjectType;
import org.dgpe.codice.common.cbclib.EstimatedOverallContractAmountType;
import org.dgpe.codice.common.cbclib.TaxExclusiveAmountType;
import org.dgpe.codice.common.cbclib.TotalAmountType;
import org.junit.jupiter.api.Test;

class MapperProcurementProjectTest {

  @Test
  void conserva_el_decimal_y_la_moneda_de_cada_importe_en_proyecto_y_lote() {
    var estimated = new EstimatedOverallContractAmountType();
    estimated.setValue(new BigDecimal("9007199254740993.123456789012345678901234567890"));
    estimated.setCurrencyID("USD");
    var total = new TotalAmountType();
    total.setValue(new BigDecimal("9999999999999999.99"));
    total.setCurrencyID("GBP");
    var taxExclusive = new TaxExclusiveAmountType();
    taxExclusive.setValue(new BigDecimal("8929000250426250.01"));
    taxExclusive.setCurrencyID("EUR");
    var budget = new BudgetAmountType();
    budget.setEstimatedOverallContractAmount(estimated);
    budget.setTotalAmount(total);
    budget.setTaxExclusiveAmount(taxExclusive);
    var source = new ProcurementProjectType();
    source.setBudgetAmount(budget);

    var project = MapperProcurementProject.getProcurementProjectFromType(null, null, source);
    assertThat(project.getEstimatedOverallContractAmount()).isEqualTo(estimated.getValue());
    assertThat(project.getEstimatedOverallContractAmountCurrency()).isEqualTo("USD");
    assertThat(project.getTotalAmount()).isEqualTo(total.getValue());
    assertThat(project.getTotalAmountCurrency()).isEqualTo("GBP");
    assertThat(project.getTaxExclusiveAmount()).isEqualTo(taxExclusive.getValue());
    assertThat(project.getTaxExclusiveAmountCurrency()).isEqualTo("EUR");

    var lotSource = new ProcurementProjectLotType();
    lotSource.setProcurementProject(source);
    var lot = MapperProcurementProjectLot.getListProcurementProjectLotFromType(null, List.of(lotSource))
        .getFirst();
    assertThat(lot.getTotalAmount()).isEqualTo(total.getValue());
    assertThat(lot.getTotalAmountCurrency()).isEqualTo("GBP");
    assertThat(lot.getTaxExclusiveAmount()).isEqualTo(taxExclusive.getValue());
    assertThat(lot.getTaxExclusiveAmountCurrency()).isEqualTo("EUR");
  }

  @Test
  void deja_en_null_el_importe_ausente_y_la_moneda_no_informada() {
    var total = new TotalAmountType();
    total.setValue(new BigDecimal("0.00"));
    var budget = new BudgetAmountType();
    budget.setTotalAmount(total);
    var source = new ProcurementProjectType();
    source.setBudgetAmount(budget);

    var project = MapperProcurementProject.getProcurementProjectFromType(null, null, source);
    assertThat(project.getTotalAmount()).isEqualTo(new BigDecimal("0.00"));
    assertThat(project.getTotalAmountCurrency()).isNull();
    assertThat(project.getTaxExclusiveAmount()).isNull();
    assertThat(project.getTaxExclusiveAmountCurrency()).isNull();
    assertThat(project.getEstimatedOverallContractAmount()).isNull();
    assertThat(project.getEstimatedOverallContractAmountCurrency()).isNull();
  }

  @Test
  void indica_si_un_importe_es_imposible_de_guardar_sin_redondearlo() {
    var total = new TotalAmountType();
    total.setValue(new BigDecimal("1E-31"));
    var budget = new BudgetAmountType();
    budget.setTotalAmount(total);
    var source = new ProcurementProjectType();
    source.setBudgetAmount(budget);

    assertThatThrownBy(() -> MapperProcurementProject.getProcurementProjectFromType(null, null, source))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("precisión de almacenamiento");
  }
}
