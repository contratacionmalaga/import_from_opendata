package local.jarios.entity.placsp;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import local.jarios.common.util.ToStringUtil;
import local.jarios.entity.auxiliares.Auditable;
import local.jarios.common.util.Constantes;
import lombok.Getter;
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
@Entity
@Table(
        name = "commodity_classification"
)
public class CommodityClassification extends Auditable {

    //
    // PROPIEDADES DEL MODELO
    //
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "item_classification_code", nullable = false, length = Constantes.TAMANO_MAXIMO_CAMPO_50)
    private String itemClassificationCode;

    //
    // RELACIONES CON ENTIDADES PADRES DE LA QUE ESTA DEPENDE
    //
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(
            name = "procurement_project_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "fk_commodityclassification_procurementproject",
                    foreignKeyDefinition =
                            "FOREIGN KEY (procurement_project_id) " +
                            "REFERENCES procurement_project(id) ON DELETE CASCADE"))
    private ProcurementProject procurementProject;

    @Override
    public String toString() {

        return "CommodityClassification: " +
                "[itemClassificationCode='" + itemClassificationCode + "']";
    }
    //
    // CONSTRUCTOR DE LA CLASE EN EL QUE SE GENERA EL UUID VERSIÓN 7
    //
    public CommodityClassification() {

        //
        this.id = Generators.timeBasedEpochGenerator().generate();
    }
}
