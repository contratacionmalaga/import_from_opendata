package local.jarios.mappers.codice;

import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.ProcurementProject;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.GregorianCalendarHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ProcurementProjectType;
import un.unece.uncefact.data.specification.corecomponenttypeschemamodule._2.TextType;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio
 */
@Slf4j
public final class MapperProcurementProject {

  private MapperProcurementProject() {
  }

  public static ProcurementProject getProcurementProjectFromType(
      ContractFolderStatus contractFolderStatus,
      ProcurementProjectType procurementProjectType) {

    //
    ProcurementProject procurementProject = new ProcurementProject();
    procurementProject.setContractFolderStatus(contractFolderStatus);

    Optional.ofNullable(procurementProjectType)
        .ifPresent(

            procurementProjectTypeVal -> {

              procurementProject.setName(
                  StringHelper.eliminarCaracteres(
                      MapperStringFromList.getStringFromListNameType(
                          procurementProjectTypeVal.getName())));

              procurementProject.setDescriptionProcurementProject(
                  StringHelper.eliminarCaracteres(
                      MapperStringFromList.getStringFromListDescriptionType(
                          procurementProjectTypeVal.getDescription())));

              Optional.ofNullable(procurementProjectTypeVal.getTypeCode())
                  .ifPresent(
                      typeCode -> procurementProject.setTypeCode(
                          ComunHelper.limitarRegistro(
                              typeCode.getValue(),
                              Constantes.TAMANO_MAXIMO_CAMPO_50)
                      )
                  );

              Optional.ofNullable(
                  procurementProjectTypeVal.getSubTypeCode()
              ).ifPresent(
                  subTypeCode -> procurementProject.setSubtypeCode(
                      ComunHelper.limitarRegistro(
                          subTypeCode.getValue(),
                          Constantes.TAMANO_MAXIMO_CAMPO_50)
                  )
              );

              Optional.ofNullable(
                  procurementProjectTypeVal.getMixContractIndicator()
              ).ifPresent(
                  mixContractIndicator -> procurementProject.setMixContractIndicator(
                      mixContractIndicator.isValue()
                  )
              );

              Optional.ofNullable(procurementProjectTypeVal.getBudgetAmount()).ifPresent(
                  budgetAmountType -> {
                    Optional.ofNullable(
                        budgetAmountType.getEstimatedOverallContractAmount()
                    ).map(
                        amount -> amount.getValue().doubleValue()
                    ).ifPresent(procurementProject::setEstimatedOverallContractAmount);

                    Optional.ofNullable(
                        budgetAmountType.getTotalAmount()
                    ).map(
                        amount -> amount.getValue().doubleValue()
                    ).ifPresent(procurementProject::setTotalAmount);

                    Optional.ofNullable(
                        budgetAmountType.getTaxExclusiveAmount()
                    ).map(
                        amount -> amount.getValue().doubleValue()
                    ).ifPresent(procurementProject::setTaxExclusiveAmount);
                  });

              Optional.ofNullable(procurementProjectTypeVal.getRealizedLocation())
                  .ifPresent(realizedLocationType -> {
                    Optional.ofNullable(realizedLocationType.getCountrySubentityCode())
                        .map(code -> ComunHelper.limitarRegistro(
                            code.getValue(),
                            Constantes.TAMANO_MAXIMO_CAMPO_50)
                        ).ifPresent(procurementProject::setCountrySubentityCode);

                    Optional.ofNullable(realizedLocationType.getCountrySubentity())
                        .map(sub -> ComunHelper.limitarRegistro(
                            sub.getValue(),
                            Constantes.TAMANO_MAXIMO_CAMPO_500)
                        ).ifPresent(procurementProject::setCountrySubentity);

                    Optional.ofNullable(realizedLocationType.getDescription())
                        .map(TextType::getValue)
                        .ifPresent(procurementProject::setDescriptionLocation);

                    Optional.ofNullable(realizedLocationType.getAddress()).flatMap(addressType ->

                                                                                       Optional.ofNullable(
                                                                                           addressType.getCountry()))

                        .ifPresent(countryType -> {

                          Optional.ofNullable(countryType.getIdentificationCode())
                              .map(sub -> ComunHelper.limitarRegistro(
                                  sub.getValue(),
                                  Constantes.TAMANO_MAXIMO_CAMPO_50)
                              ).ifPresent(procurementProject::setCountryIdentificationCode);

                          Optional.ofNullable(countryType.getName())
                              .map(sub -> ComunHelper.limitarRegistro(
                                  sub.getValue(),
                                  Constantes.TAMANO_MAXIMO_CAMPO_500)
                              ).ifPresent(procurementProject::setCountryIdentificatonName);
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
                                        startDateType.getValue())
                            )
                            .ifPresent(
                                startDate -> {
                                  LocalTime startTime = Optional.ofNullable(
                                          plannedPeriodType.getStartTime())
                                      .map(
                                          startTimeType ->
                                              GregorianCalendarHelper.getTimeFromXMLGregorianCalendar(
                                                  startTimeType.getValue()
                                              )
                                      ).orElse(LocalTime.MIDNIGHT);
                                  procurementProject.setStartDateTime(
                                      LocalDateTime.of(startDate, startTime));
                                }
                            );

                        // EndDate + EndTime
                        Optional.ofNullable(plannedPeriodType.getEndDate())
                            .map(endDateType ->
                                     GregorianCalendarHelper.getDateFromXMLGregorianCalendar(
                                         endDateType.getValue())
                            ).ifPresent(endDate -> {
                                          LocalTime endTime = Optional.ofNullable(plannedPeriodType.getEndTime())
                                              .map(endTimeType ->
                                                       GregorianCalendarHelper.getTimeFromXMLGregorianCalendar(
                                                           endTimeType.getValue()))
                                              .orElse(LocalTime.MIDNIGHT);
                                          procurementProject.setEndDateTime(LocalDateTime.of(endDate, endTime));
                                        }
                            );

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
                                }
                            );
                      }
                  );

              Optional.ofNullable(procurementProjectTypeVal.getContractExtension())
                  .ifPresent(contractExtensionType ->
                                 procurementProject.setOptionsDescription(
                                     StringHelper.eliminarCaracteres(
                                         MapperStringFromList.getStringFromListOptionsDescriptionType(
                                             contractExtensionType.getOptionsDescription())))
                  );

              // ListRequiredCommodityClassification
              procurementProject.setRequiredCommodityClassificationList(
                  MapperCommodityClassification.getListCommodityClassificationFromType(
                      procurementProject,
                      null,
                      procurementProjectType.getRequiredCommodityClassification()
                  )
              );
            }
        );


    return procurementProject;
  }
}
