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
    name = "procurement_project"
)
public class ProcurementProject extends Auditable {

  //
  // PROPIEDADES DEL MODELO
  //
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "name", nullable = false, columnDefinition = "TEXT")
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String typeCode;

  @Column(name = "subtype_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String subtypeCode;

  @Column(name = "mix_contract_indicator")
  private Boolean mixContractIndicator;

  // =========================================================================
  // RELACIONES PADRES
  // =========================================================================
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contract_folder_status_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_procurementproject_contractfolderstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (contract_folder_status_id) " +
                  "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus contractFolderStatus;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "preliminary_market_consultation_status_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_procurementproject_preliminarymarketconsultationstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (preliminary_market_consultation_status_id) " +
                  "REFERENCES preliminary_market_consultation_status(id) ON DELETE CASCADE"))
  private PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus;

  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "procurement_project_lot_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_procurementproject_procurementprojectlot",
          foreignKeyDefinition =
              "FOREIGN KEY (procurement_project_lot_id) " +
                  "REFERENCES procurement_project_lot(id) ON DELETE CASCADE"))
  private ProcurementProjectLot procurementProjectLot;

  // =========================================================================
  // RELACIONES HIJAS
  // =========================================================================
  @OneToOne(mappedBy = "procurementProject", cascade = CascadeType.ALL, orphanRemoval = true)
  private BudgetAmount budgetAmount;

  @OneToMany(mappedBy = "procurementProject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<CommodityClassification> requiredCommodityClassification = new ArrayList<>();

  @OneToOne(mappedBy = "procurementProject", cascade = CascadeType.ALL, orphanRemoval = true)
  private Location realizedLocation;

  @OneToOne(mappedBy = "procurementProject", cascade = CascadeType.ALL, orphanRemoval = true)
  private Period plannedPeriod;

  @OneToOne(mappedBy = "procurementProject", cascade = CascadeType.ALL, orphanRemoval = true)
  private ContractExtension contractExtension;

  // =========================================================================
  // MÉTODOS AUXILIARES
  // =========================================================================
  /**
   * Devuelve una representación en cadena del objeto con los valores principales de la entidad.
   *
   * @return cadena con los valores de {@code name}, {@code description}, {@code typeCode},
   *        {@code subtypeCode}, {@code mixContractIndicator}.
   */
  @Override
  public String toString() {

    return "ProcurementProject: " +
        "[name='" + name + "', " +
        "description='" + description + "', " +
        "typeCode='" + typeCode + "', " +
        "subtypeCode='" + subtypeCode + "', " +
        "mixContractIndicator='" + mixContractIndicator + "']";
  }
}
