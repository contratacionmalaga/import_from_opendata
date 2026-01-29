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

import java.time.LocalDate;
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
    name = "tender_result"
)

//
public class TenderResult extends AuditableCreatedAt {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // 4.35.1 Tipo de resultado
  @Column(name = "result_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String resultCode;

  // 4.35.5 Motivación - Descripción textual
  @Column(name = "description_tender_result", columnDefinition = "TEXT")
  private String descriptionTenderResult;

  // 4.35.5 Motivación - Fecha del acuerdo
  @Column(name = "award_date")
  private LocalDate awardDate;

  // 4.35.4 Número de licitadores participantes
  @Column(name = "received_tender_quantity")
  private Double receivedTenderQuantity;

  // 4.35.6 Ofertas recibidas - Precio de la oferta más baja
  @Column(name = "lower_tender_amount_quantity")
  private Double lowerTenderAmountQuantity;

  // 4.35.6 Ofertas recibidas - Precio de la oferta más alta
  @Column(name = "higher_tender_amount_quantity")
  private Double higherTenderAmountQuantity;

  // 4.35.7 Información sobre el contrato - Fecha de inicio del contrato
  @Column(name = "start_date", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private LocalDate startDate;

  // 4.35.6 Ofertas recibidas - Número de ofertas recibidas de pymes
  @Column(name = "smes_received_tender_quantity")
  private Double sMEsReceivedTenderQuantity;

  // 4.35.6 Ofertas recibidas - Número de ofertas recibidas de extranjeros comunitarios (UE)
  @Column(name = "eu_nationals_received_tender_quantity")
  private Double eUNationalsReceivedTenderQuantity;

  // 4.35.6 Ofertas recibidas - Número de ofertas recibidas de extranjeros comunitarios (no UE)
  @Column(name = "noneu_nationals_received_tender_quantity")
  private Double nonEUNationalsReceivedTenderQuantity;

  // 4.35.6 Ofertas recibidas - Es PYME el adjudicatario
  @Column(name = "sme_awarded_indicator")
  private Boolean sMEAwardedIndicator;

  // 4.35.7 Información sobre el contratista - Nacionalidad del contratista (código)
  @Column(name = "awarded_owner_nationality_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String awardedOwnerNationalityCode;

  // 4.35.6 Ofertas recibidas - Se han excluído ofertas por ser anormalmente bajas
  @Column(name = "abnormally_low_tenders_indicator")
  private Boolean abnormallyLowTendersIndicator;

  /**
   * Contract
   */
  @Column(name = "id_contract", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String idContract;

  @Column(name = "issue_date")
  private LocalDate issueDate;

  /**
   * AwardedTenderedProject
   */
  @Column(name = "procurement_project_lot_id", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String procurementProjectLotId;

  @Column(name = "payable_amount")
  private Double payableAmount;

  @Column(name = "tax_exclusive_amount")
  private Double taxExclusiveAmount;

  @Column(name = "tax_inclusive_amount")
  private Double taxInclusiveAmount;

  /**
   * SubcontractTerms
   */
  @Column(name = "rate")
  private Double rate;

  @Column(name = "subcontract_terms_description", columnDefinition = "TEXT")
  private String subcontractTermsDescription;

  // PartyName
  @Column(name = "party_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String partyName;

  // PhysicalLocation
  @Column(name = "country_subentity_code", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String countrySubentityCode;

  @Column(name = "country_subentity", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String countrySubentity;

  // Address
  @Column(name = "city_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String cityName;

  @Column(name = "postal_zone", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String postalZone;

  // Contact
  @Column(name = "contact_name", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String contactMame;

  @Column(name = "contact_telephone", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String contactTelephone;

  @Column(name = "contact_electronic_mail", length = Constantes.TAMANO_MAXIMO_CAMPO_500)
  private String contactElectronicMail;

  // Party Identification
  @Column(name = "id_plataforma", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String idPlataforma;

  @Column(name = "nif", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String nif;

  @Column(name = "otros", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String otros;

  // ////////////////////
  // RELACIONES PADRE
  // ////////////////////
  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contract_folder_status_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_tenderresult_contractfolderstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (contract_folder_status_id) " +
                  "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus contractFolderStatus;

  @Override
  public String toString() {

    return "TenderResult: " +
        "[resultCode='" + resultCode + "', " +
        "descriptionTenderResult='" + descriptionTenderResult + "', " +
        "awardDate='" + awardDate + "', " +
        "receivedTenderQuantity='" + receivedTenderQuantity + "', " +
        "lowerTenderAmountQuantity='" + lowerTenderAmountQuantity + "', " +
        "higherTenderAmountQuantity='" + higherTenderAmountQuantity + "', " +
        "startDate='" + startDate + "', " +
        "sMEsReceivedTenderQuantity='" + sMEsReceivedTenderQuantity + "', " +
        "eUNationalsReceivedTenderQuantity='" + eUNationalsReceivedTenderQuantity + "', " +
        "nonEUNationalsReceivedTenderQuantity='" + nonEUNationalsReceivedTenderQuantity + "', " +
        "sMEAwardedIndicator='" + sMEAwardedIndicator + "', " +
        "awardedOwnerNationalityCode='" + awardedOwnerNationalityCode + "', " +
        "abnormallyLowTendersIndicator='" + abnormallyLowTendersIndicator + "']";
  }
}
