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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import local.jarios.common.util.Constantes;
import local.jarios.entity.auxiliares.AuditableCreatedAt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    name = "procurement_project_lot"
)

// 4.11 Lotes
public class ProcurementProjectLot extends AuditableCreatedAt {

  //
  // PROPIEDADES DEL MODELO
  //

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // 4.11.1 Número de lote
  @Column(name = "id_lote", length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String idLote;

  //
  // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
  //

  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "contract_folder_status_id",
      nullable = false,
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_procurementprojectlot_contractfolderstatus",
          foreignKeyDefinition =
              "FOREIGN KEY (contract_folder_status_id) " +
                  "REFERENCES contract_folder_status(id) ON DELETE CASCADE"))
  private ContractFolderStatus contractFolderStatus;

  //
  // RELACIONES CON ENTIDADES HIJAS DEPENDIENTE DE ESTA
  //

  @OneToOne(mappedBy = "procurementProjectLot", cascade = CascadeType.ALL, orphanRemoval = true)
  private ProcurementProject procurementProject;

  @OneToOne(mappedBy = "procurementProjectLot", cascade = CascadeType.ALL, orphanRemoval = true)
  private TenderingTerms tenderingTerms;

  @Override
  public String toString() {

    return "ProcurementProjectLot: " +
        "[idLote='" + idLote + "']";
  }
}
