package local.jarios.entity.placsp;

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
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Description: Importaciones de Ficheros Excel desde Internet
 * Author: Juan Antonio
 * Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "financial_guarantee"
)

// 4.29 Garantías requeridas
public class FinancialGuarantee extends Auditable {

  //
  //
  //
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Tipo de Garantía
  @Column(name = "guarantee_type_code", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String guaranteeTypeCode;

  // Porcentaje de la garantía
  @Column(name = "amount_rate")
  private Double amountRate;

  // Importe
  @Column(name = "liability_amount")
  private Double liabilityAmount;

  //
  //
  //
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "tendering_terms_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_financialguarantee_tenderingterms",
          foreignKeyDefinition =
              "FOREIGN KEY (tendering_terms_id) " +
                  "REFERENCES tendering_terms(id) ON DELETE CASCADE"))
  private TenderingTerms tenderingTerms;

  @Override
  public String toString() {

    return "FinancialGuarantee: " +
        "[guaranteeTypeCode='" + guaranteeTypeCode + "', " +
        "amountRate='" + amountRate + "', " +
        "liabilityAmount='" + liabilityAmount + "']";
  }
}
