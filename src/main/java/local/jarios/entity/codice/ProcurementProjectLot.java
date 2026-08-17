package local.jarios.entity.codice;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Description: Importaciones de Ficheros Excel desde Internet Author: Juan Antonio Date: 04/06/2024
 * Team: Juan Antonio
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "procurement_project_lot")
// 4.11 Lotes
public class ProcurementProjectLot extends AuditableCreatedAt {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // 4.11.1 Número de lote
  @Column(name = "lote", length = TamanoCampos.TAMANO_50)
  private String lote;

  @Column(name = "name", nullable = false, columnDefinition = "TEXT")
  private String name;

  @Column(name = "total_amount")
  private Double totalAmount;

  @Column(name = "tax_exclusive_amount")
  private Double taxExclusiveAmount;

  @Column(name = "country_subentity", length = TamanoCampos.TAMANO_500)
  private String countrySubentity;

  @Column(name = "country_subentity_code", length = TamanoCampos.TAMANO_50)
  private String countrySubentityCode;

  @Column(name = "country_identification_code", length = TamanoCampos.TAMANO_50)
  private String countryIdentificationCode;

  @Column(name = "country_identification_name", length = TamanoCampos.TAMANO_500)
  private String countryIdentificatonName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contract_folder_status_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey =
          @ForeignKey(
              name = "fk_procurementprojectlot_contractfolderstatus",
              foreignKeyDefinition =
                  "FOREIGN KEY (contract_folder_status_id) "
                      + "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus contractFolderStatus;

  @OneToMany(
      mappedBy = "procurementProjectLot",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<CommodityClassification> requiredCommodityClassificationList = new ArrayList<>();

  @OneToOne(mappedBy = "procurementProjectLot", cascade = CascadeType.ALL, orphanRemoval = true)
  private TenderingTerms tenderingTerms;
}
