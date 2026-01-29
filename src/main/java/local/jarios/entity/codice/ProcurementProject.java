package local.jarios.entity.codice;

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
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class ProcurementProject extends AuditableCreatedAt {

  //
  // PROPIEDADES DEL MODELO
  //
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "name", nullable = false, columnDefinition = "TEXT")
  private String name;

  @Column(name = "description_procurement_project", columnDefinition = "TEXT")
  private String descriptionProcurementProject;

  @Column(name = "type_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String typeCode;

  @Column(name = "subtype_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String subtypeCode;

  @Column(name = "mix_contract_indicator")
  private Boolean mixContractIndicator;

  @Column(name = "estimated_overall_contract_amount")
  private Double estimatedOverallContractAmount;

  @Column(name = "total_amount")
  private Double totalAmount;

  @Column(name = "tax_exclusive_amount")
  private Double taxExclusiveAmount;

  @Column(name = "country_subentity", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String countrySubentity;

  @Column(name = "country_subentity_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String countrySubentityCode;

  @Column(name = "country_identification_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String countryIdentificationCode;

  @Column(name = "country_identification_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String countryIdentificatonName;

  @Column(name = "description_location", columnDefinition = "TEXT")
  private String descriptionLocation;

  @Column(name = "start_date_time")
  private LocalDateTime startDateTime;

  @Column(name = "end_date_time")
  private LocalDateTime endDateTime;

  @Column(name = "description_period", columnDefinition = "TEXT")
  private String descriptionPeriod;

  @Column(name = "measure_value")
  private BigDecimal measureValue;

  @Column(name = "measure_unit_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String measureUnitCode;

  @Column(name = "options_description", columnDefinition = "TEXT")
  private String optionsDescription;

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

//  @OneToOne(
//      fetch = FetchType.LAZY)
//  @JoinColumn(
//      name = "preliminary_market_consultation_status_id",
//      referencedColumnName = "id",
//      foreignKey = @ForeignKey(
//          name = "fk_procurementproject_preliminarymarketconsultationstatus",
//          foreignKeyDefinition =
//              "FOREIGN KEY (preliminary_market_consultation_status_id) " +
//                  "REFERENCES preliminary_market_consultation_status(id) ON DELETE CASCADE"))
//  private PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus;


  // =========================================================================
  // RELACIONES HIJAS
  // =========================================================================
  @OneToMany(mappedBy = "procurementProject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<CommodityClassification> requiredCommodityClassificationList = new ArrayList<>();

  // =========================================================================
  // MÉTODOS AUXILIARES
  // =========================================================================

  /**
   * Devuelve una representación en cadena del objeto con los valores principales de la entidad.
   *
   * @return cadena con los valores de {@code name}, {@code description}, {@code typeCode},
   * {@code subtypeCode}, {@code mixContractIndicator}.
   */
  @Override
  public String toString() {

    return "ProcurementProject: " +
        "[name='" + name + "', " +
        "typeCode='" + typeCode + "', " +
        "subtypeCode='" + subtypeCode + "', " +
        "mixContractIndicator='" + mixContractIndicator + "']";
  }
}
