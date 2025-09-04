package local.jarios.entity.placsp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Description: Importaciones de Ficheros Excel desde Internet Author: Juan Antonio Date: 04/06/2024
 * Team: Juan Antonio
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "tendering_terms"
)

public class TenderingTerms extends Auditable {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // 4.28 Condiciones de licitación
  // - RequiredCurriculaIndicator
  @Column(name = "required_curricula_indicator")
  private Boolean requiredCurriculaIndicator;

  // 4.28 Condiciones de licitación
  // - VariantConstraintIndicator
  @Column(name = "variant_constraint_indicator")
  private Boolean variantConstraintIndicator;

  // 4.28 Condiciones de licitación
  // - PriceRevisionFormulaDescription

  @Column(name = "price_revision_formula_description", columnDefinition = "TEXT")
  private String priceRevisionFormulaDescription;

  // 4.28.1 Programas de financiación europea
  // - Lista de Códigos de Financiación Europea
  @Column(name = "funding_program_code", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
  private String fundingProgramCode;

  // 4.28.1 Programas de financiación europea
  // - Descripción de programas de financiación: Descripción textual de los
  // programas que financian este contrato
  @Column(name = "funding_program", length = Constantes.TAMANO_MAXIMO_CAMPO_2500)
  private String fundingProgram;

  // 4.29 Garantías requeridas
  // - Lista de garantías requeridas
  @Column(name = "procurement_national_legislation_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String procurementNationalLegislationCode;

  @Column(name = "procurement_legislation_document_reference", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String procurementLegislationDocumentReference;

  // 4.46 Número de recursos presentados
  // Indicador numérico introducido por el órgano de contratación que representa
  // el número de recursos presentados
  // en la licitación
  @Column(name = "received_appeal_quantity")
  private Double receivedAppealQuantity;

  // 4.47 Condiciones de ejecución
  // Uso del pedido electrónico
  @Column(name = "eordering_indicator")
  private Boolean eorderingIndicator;

  // 4.47 Condiciones de ejecución
  // Aceptación de factura electrónica
  @Column(name = "epaymentmeans_indicator")
  private Boolean epaymentMeansIndicator;

  // 4.47 Condiciones de ejecución
  // Uso de pago electrónico
  @Column(name = "electronic_invoicing_indicator")
  private Boolean electronicInvoicingIndicator;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contract_folder_status_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_tenderingterms_contractfolderstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (contract_folder_status_id) " +
                  "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus contractFolderStatus;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "procurement_project_lot_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_tenderingterms_procurementprojectlot",
          foreignKeyDefinition =
              "FOREIGN KEY (procurement_project_lot_id) " +
                  "REFERENCES procurement_project_lot(id) ON DELETE CASCADE"))
  private ProcurementProjectLot procurementProjectLot;


  @OneToOne(mappedBy = "tenderingTerms", cascade = CascadeType.ALL, orphanRemoval = true)
  private AwardingTerms awardingTerms;

  @OneToOne(mappedBy = "tenderingTerms", cascade = CascadeType.ALL, orphanRemoval = true)
  private TendererQualificationRequest tendererQualificationRequest;

  @OneToMany(mappedBy = "tenderingTerms", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<SubcontractTerms> listAllowedSubcontractTerms = new ArrayList<>();

  @OneToMany(mappedBy = "tenderingTerms", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<ContractExecutionRequirement> listContractExecutionRequirement = new ArrayList<>();

  @OneToMany(mappedBy = "tenderingTerms", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<FinancialGuarantee> listFinancialGuarantee = new ArrayList<>();

  @OneToOne(mappedBy = "tenderingTerms", cascade = CascadeType.ALL, orphanRemoval = true)
  private TenderRecipientParty tenderRecipientParty;

  @Override
  public String toString() {

    return "TenderingTerms: " +
        "requiredCurriculaIndicator='" + requiredCurriculaIndicator + "', " +
        "variantConstraintIndicator='" + variantConstraintIndicator + "', " +
        "priceRevisionFormulaDescription='" + priceRevisionFormulaDescription + "', " +
        "fundingProgramCode='" + fundingProgramCode + "', " +
        "fundingProgram='" + fundingProgram + "', " +
        "procurementNationalLegislationCode='" + procurementNationalLegislationCode + "', " +
        "procurementLegislationDocumentReference='" + procurementLegislationDocumentReference + "', " +
        "receivedAppealQuantity='" + receivedAppealQuantity + "', " +
        "eorderingIndicator='" + eorderingIndicator + "', " +
        "epaymentMeansIndicator='" + epaymentMeansIndicator + "', " +
        "electronicInvoicingIndicator='" + electronicInvoicingIndicator + "']";
  }
}
