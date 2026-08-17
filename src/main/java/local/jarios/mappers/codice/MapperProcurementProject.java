package local.jarios.mappers.codice;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.PreliminaryMarketConsultationStatus;
import local.jarios.entity.codice.ProcurementProject;
import local.jarios.helpers.GregorianCalendarHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ProcurementProjectType;
import un.unece.uncefact.data.specification.corecomponenttypeschemamodule._2.TextType;

/** Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio */
@Slf4j
public final class MapperProcurementProject {

  private MapperProcurementProject() {}

  public static ProcurementProject getProcurementProjectFromType(
      ContractFolderStatus contractFolderStatus,
      PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus,
      ProcurementProjectType procurementProjectType) {

    //
    ProcurementProject procurementProject = new ProcurementProject();
    procurementProject.setContractFolderStatus(contractFolderStatus);
    procurementProject.setPreliminaryMarketConsultationStatus(preliminaryMarketConsultationStatus);

    Optional.ofNullable(procurementProjectType)
        .ifPresent(
            procurementProjectTypeVal -> {
              procurementProject.setName(
                  Optional.of(
                          StringHelper.eliminarCaracteres(
                              MapperStringFromList.getStringFromListNameType(
                                  procurementProjectTypeVal.getName())))
                      .filter(s -> !s.isBlank())
                      .orElse("SIN_NOMBRE"));

              procurementProject.setDescriptionProcurementProject(
                  StringHelper.eliminarCaracteres(
                      StringHelper.limit(
                          MapperStringFromList.getStringFromListDescriptionType(
                              procurementProjectTypeVal.getDescription()),
                          TamanoCampos.TAMANO_2500)));

              Optional.ofNullable(procurementProjectTypeVal.getTypeCode())
                  .ifPresent(
                      typeCode ->
                          procurementProject.setTypeCode(
                              StringHelper.limit(typeCode.getValue(), TamanoCampos.TAMANO_50)));

              Optional.ofNullable(procurementProjectTypeVal.getSubTypeCode())
                  .ifPresent(
                      subTypeCode ->
                          procurementProject.setSubtypeCode(
                              StringHelper.limit(subTypeCode.getValue(), TamanoCampos.TAMANO_50)));

              Optional.ofNullable(procurementProjectTypeVal.getMixContractIndicator())
                  .ifPresent(
                      mixContractIndicator ->
                          procurementProject.setMixContractIndicator(
                              mixContractIndicator.isValue()));

              Optional.ofNullable(procurementProjectTypeVal.getBudgetAmount())
                  .ifPresent(
                      budgetAmountType -> {
                        Optional.ofNullable(budgetAmountType.getEstimatedOverallContractAmount())
                            .map(amount -> amount.getValue().doubleValue())
                            .ifPresent(procurementProject::setEstimatedOverallContractAmount);

                        Optional.ofNullable(budgetAmountType.getTotalAmount())
                            .map(amount -> amount.getValue().doubleValue())
                            .ifPresent(procurementProject::setTotalAmount);

                        Optional.ofNullable(budgetAmountType.getTaxExclusiveAmount())
                            .map(amount -> amount.getValue().doubleValue())
                            .ifPresent(procurementProject::setTaxExclusiveAmount);
                      });

              Optional.ofNullable(procurementProjectTypeVal.getRealizedLocation())
                  .ifPresent(
                      realizedLocationType -> {
                        Optional.ofNullable(realizedLocationType.getCountrySubentityCode())
                            .map(
                                code -> StringHelper.limit(code.getValue(), TamanoCampos.TAMANO_50))
                            .ifPresent(procurementProject::setCountrySubentityCode);

                        Optional.ofNullable(realizedLocationType.getCountrySubentity())
                            .map(sub -> StringHelper.limit(sub.getValue(), TamanoCampos.TAMANO_500))
                            .ifPresent(procurementProject::setCountrySubentity);

                        Optional.ofNullable(realizedLocationType.getDescription())
                            .map(TextType::getValue)
                            .ifPresent(procurementProject::setDescriptionLocation);

                        Optional.ofNullable(realizedLocationType.getAddress())
                            .flatMap(addressType -> Optional.ofNullable(addressType.getCountry()))
                            .ifPresent(
                                countryType -> {
                                  Optional.ofNullable(countryType.getIdentificationCode())
                                      .map(
                                          sub ->
                                              StringHelper.limit(
                                                  sub.getValue(), TamanoCampos.TAMANO_50))
                                      .ifPresent(procurementProject::setCountryIdentificationCode);

                                  Optional.ofNullable(countryType.getName())
                                      .map(
                                          sub ->
                                              StringHelper.limit(
                                                  sub.getValue(), TamanoCampos.TAMANO_500))
                                      .ifPresent(procurementProject::setCountryIdentificationName);
                                });
                      });

              Optional.ofNullable(procurementProjectTypeVal.getPlannedPeriod())
                  .ifPresent(
                      plannedPeriodType -> {

                        // StartDate + StartTime
                        Optional.ofNullable(plannedPeriodType.getStartDate())
                            .map(
                                startDateType ->
                                    GregorianCalendarHelper.getDateFromXMLGregorianCalendar(
                                        startDateType.getValue()))
                            .ifPresent(
                                startDate -> {
                                  LocalTime startTime =
                                      Optional.ofNullable(plannedPeriodType.getStartTime())
                                          .map(
                                              startTimeType ->
                                                  GregorianCalendarHelper
                                                      .getTimeFromXMLGregorianCalendar(
                                                          startTimeType.getValue()))
                                          .orElse(LocalTime.MIDNIGHT);
                                  procurementProject.setStartDateTime(
                                      LocalDateTime.of(startDate, startTime));
                                });

                        // EndDate + EndTime
                        Optional.ofNullable(plannedPeriodType.getEndDate())
                            .map(
                                endDateType ->
                                    GregorianCalendarHelper.getDateFromXMLGregorianCalendar(
                                        endDateType.getValue()))
                            .ifPresent(
                                endDate -> {
                                  LocalTime endTime =
                                      Optional.ofNullable(plannedPeriodType.getEndTime())
                                          .map(
                                              endTimeType ->
                                                  GregorianCalendarHelper
                                                      .getTimeFromXMLGregorianCalendar(
                                                          endTimeType.getValue()))
                                          .orElse(LocalTime.MIDNIGHT);
                                  procurementProject.setEndDateTime(
                                      LocalDateTime.of(endDate, endTime));
                                });

                        // Description
                        procurementProject.setDescriptionPeriod(
                            StringHelper.eliminarCaracteres(
                                MapperStringFromList.getStringFromListDescriptionType(
                                    plannedPeriodType.getDescription())));

                        Optional.ofNullable(plannedPeriodType.getDurationMeasure())
                            .ifPresent(
                                durationMeasureType -> {
                                  Optional.ofNullable(durationMeasureType.getUnitCode())
                                      .ifPresent(procurementProject::setMeasureUnitCode);

                                  Optional.ofNullable(durationMeasureType.getValue())
                                      .ifPresent(procurementProject::setMeasureValue);
                                });
                      });

              Optional.ofNullable(procurementProjectTypeVal.getContractExtension())
                  .ifPresent(
                      contractExtensionType ->
                          procurementProject.setOptionsDescription(
                              StringHelper.eliminarCaracteres(
                                  MapperStringFromList.getStringFromListOptionsDescriptionType(
                                      contractExtensionType.getOptionsDescription()))));

              // ListRequiredCommodityClassification
              procurementProject.setRequiredCommodityClassificationList(
                  MapperCommodityClassification.getListCommodityClassificationFromType(
                      procurementProject,
                      null,
                      procurementProjectTypeVal.getRequiredCommodityClassification()));
            });

    return procurementProject;
  }
}
