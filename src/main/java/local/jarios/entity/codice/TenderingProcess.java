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
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
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
    name = "tendering_process"
)

public class TenderingProcess extends AuditableCreatedAt {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // 4.12 Tipo de procedimiento
  @Column(name = "procedure_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String procedureCode;

  // 4.13 Sistema de contratación
  @Column(name = "contracting_system_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String contractingSystemCode;

  // 4.14 Tipo de tramitación
  @Column(name = "urgency_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String urgencyCode;

  // 4.15 Forma de la presentación de la oferta
  @Column(name = "submission_method_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String submissionMethodCode;

  // 4.34 Limitación del número de licitadores - Número de lotes a los que se debe ofertar
  @Column(name = "part_presentation_code", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String partPresentationCode;

  // 4.34 Limitación del número de licitadores - Número de lotes a los que se puede ofertar
  @Column(name = "maximum_lot_presentation_quantity")
  private Double maximumLotPresentationQuantity;

  // 4.34 Limitación del número de licitadores - Número máximo de lotes que se puede adjudicar a un licitador
  @Column(name = "maximun_tenderer_awarded_lot_quantity")
  private Double maximunTendererAwardedLotQuantity;

  // 4.34 Limitación del número de licitadores -
  // El poder adjudicador se reserva el derecho de adjudicar contratos que combinen lotes
  @Column(name = "lots_combination_contracting_authority_rights", columnDefinition = "TEXT")
  private String lotsCombinationContractingAuthorityRights;

  // 4.44 Contrato SARA/Umbra
  // Indica si la licitación es de Regulación armonizada para la directiva 24/2014
  // o si supera el umbral comunitario en caso de tratarse de otras directivas
  @Column(name = "overthreshold_indicator")
  private Boolean overThresholdIndicator;

  // Descripción
  @Column(name = "tendering_process_description", columnDefinition = "TEXT")
  private String tenderingProcessDescription;

  // Fecha y Hora límite para obtener los pliegos
  @Column(name = "document_availability_period")
  private LocalDateTime documentAvailabilityPeriod;

  // Fecha y hora límites para la presentación de oferta
  @Column(name = "tender_submission_deadline_period")
  private LocalDateTime tenderSubmissionDeadlinePeriod;

  // Indicador de subasta electrónica.
  @Column(name = "auction_constraint_indicator")
  private Boolean auctionConstraintIndicator;

  // Descripción textual de los criterios objetivos para la selección de un número limitado de
  // candidatos.
  @Column(name = "economic_short_list_description", columnDefinition = "TEXT")
  private String economicShortListDescription;

  // Número previsto de operadores económicos para la creación de la lista corta de candidatos.
  @Column(name = "expected_quantity")
  private Double expectedQuantity;

  // Número máximo de empresarios u operadores económicos que pueden ser seleccionados para la lista
  // corta de candidatos.
  @Column(name = "maximum_quantity")
  private Double maximumQuantity;

  // Número mínimo de empresarios u operadores económicos que deben ser seleccionados para la lista
  // corta de candidatos.
  @Column(name = "minimum_quantity")
  private Double minimumQuantity;

  //
  @OneToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contract_folder_status_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_tenderingprocess_contractfolderstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (contract_folder_status_id) " +
                  "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus contractFolderStatus;

  @Override
  public String toString() {

    return "TenderingProcess: " +
        "procedureCode='" + procedureCode + "', " +
        "contractingSystemCode='" + contractingSystemCode + "', " +
        "urgencyCode='" + urgencyCode + "', " +
        "submissionMethodCode='" + submissionMethodCode + "', " +
        "partPresentationCode='" + partPresentationCode + "', " +
        "maximumLotPresentationQuantity='" + maximumLotPresentationQuantity + "', " +
        "maximunTendererAwardedLotQuantity='" + maximunTendererAwardedLotQuantity + "', " +
        "lotsCombinationContractingAuthorityRights='" + lotsCombinationContractingAuthorityRights + "', " +
        "overThresholdIndicator='" + overThresholdIndicator + "', " +
        "documentAvailabilityPeriod='" + documentAvailabilityPeriod + "', " +
        "tenderSubmissionDeadlinePeriod='" + tenderSubmissionDeadlinePeriod + "']";
  }
}
