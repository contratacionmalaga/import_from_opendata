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
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
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
    name = "notice_info"
)
public class NoticeInfo extends AuditableCreatedAt {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "notice_type_code", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String noticeTypeCode;

  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contract_folder_status_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_noticeinfo_contractfolderstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (contract_folder_status_id) " +
                  "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus contractFolderStatus;

  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "preliminary_market_consultation_status_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_noticeinfo_preliminarymarketconsultationstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (preliminary_market_consultation_status_id) " +
                  "REFERENCES preliminary_market_consultation_status(id) ON DELETE CASCADE"))
  private PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus;

  //
  //
  //
  @OneToMany(mappedBy = "noticeInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<AdditionalPublicationStatus> listAdditionalPublicationStatus = new ArrayList<>();

  @Override
  public String toString() {

    return "NoticeInfo: " +
        "[noticeTypeCode='" + noticeTypeCode + "']";
  }
}
