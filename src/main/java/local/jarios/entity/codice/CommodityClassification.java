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
    name = "commodity_classification"
)
public class CommodityClassification extends AuditableCreatedAt {

  //
  // PROPIEDADES DEL MODELO
  //
  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "item_classification_code", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
  private String itemClassificationCode;

  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "procurement_project_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_cpv_procurementproject",
          foreignKeyDefinition =
              "FOREIGN KEY (procurement_project_id) " +
                  "REFERENCES procurement_project(id) ON DELETE CASCADE"))
  private ProcurementProject procurementProject;

  @ManyToOne(
      fetch = FetchType.LAZY)
  @JoinColumn(
      name = "procurement_project_lot_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(
          name = "fk_cpv_procurementprojectlot",
          foreignKeyDefinition =
              "FOREIGN KEY (procurement_project_lot_id) " +
                  "REFERENCES procurement_project_lot(id) ON DELETE CASCADE"))
  private ProcurementProjectLot procurementProjectLot;

  @Override
  public String toString() {

    return "CommodityClassification: " +
        "[itemClassificationCode='" + itemClassificationCode + "']";
  }
}
