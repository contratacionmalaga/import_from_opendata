package local.jarios.mappers;

import local.jarios.entity.placsp.CommodityClassification;
import local.jarios.entity.placsp.ProcurementProject;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.CommodityClassificationType;
import org.dgpe.codice.common.cbclib.ItemClassificationCodeType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperCommodityClassification {

  private MapperCommodityClassification() {
  }

  public static List<CommodityClassification> getListCommodityClassification(
      ProcurementProject procurementProject,
      List<CommodityClassificationType> listCommodityClassificationType) {

    //
    return listCommodityClassificationType.stream()
        .map(type -> getCommodityClassificationFromType(procurementProject, type))
        .toList();
  }

  public static CommodityClassification getCommodityClassificationFromType(
      ProcurementProject procurementProject,
      CommodityClassificationType commodityClassificationType) {

    //
    CommodityClassification commodityClassification = new CommodityClassification();
    commodityClassification.setProcurementProject(procurementProject);

    Optional.ofNullable(commodityClassificationType.getItemClassificationCode())
        .map(ItemClassificationCodeType::getValue)
        .ifPresent(commodityClassification::setItemClassificationCode);

    return commodityClassification;
  }
}
