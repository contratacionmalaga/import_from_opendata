package local.jarios.mappers.codice;

import java.util.List;
import java.util.Optional;
import local.jarios.entity.codice.CommodityClassification;
import local.jarios.entity.codice.ProcurementProject;
import local.jarios.entity.codice.ProcurementProjectLot;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.CommodityClassificationType;
import org.dgpe.codice.common.cbclib.ItemClassificationCodeType;

/** Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio. */
@Slf4j
public final class MapperCommodityClassification {

  private MapperCommodityClassification() {}

  public static List<CommodityClassification> getListCommodityClassificationFromType(
      ProcurementProject procurementProject,
      ProcurementProjectLot procurementProjectLot,
      List<CommodityClassificationType> listCommodityClassificationType) {

    //
    return listCommodityClassificationType.stream()
        .map(
            type ->
                getCommodityClassificationFromType(procurementProject, procurementProjectLot, type))
        .toList();
  }

  private static CommodityClassification getCommodityClassificationFromType(
      ProcurementProject procurementProject,
      ProcurementProjectLot procurementProjectLot,
      CommodityClassificationType commodityClassificationType) {

    //
    CommodityClassification commodityClassification = new CommodityClassification();
    commodityClassification.setProcurementProject(procurementProject);
    commodityClassification.setProcurementProjectLot(procurementProjectLot);

    Optional.ofNullable(commodityClassificationType.getItemClassificationCode())
        .map(ItemClassificationCodeType::getValue)
        .ifPresent(commodityClassification::setItemClassificationCode);

    return commodityClassification;
  }
}
