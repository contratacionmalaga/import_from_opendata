package local.jarios.mappers.codice;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import local.jarios.codice.LegalMonetaryTotal;
import local.jarios.codice.LegalMonetaryTotalMapper;
import local.jarios.codice.MeassureMapper;
import local.jarios.codice.Measure;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.ContractModification;
import local.jarios.helpers.GregorianCalendarHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import org.dgpe.codice.common.caclib.ContractModificationType;
import org.dgpe.codice.common.caclib.LegalMonetaryTotalType;
import org.dgpe.codice.common.cbclib.ContractIDType;
import org.dgpe.codice.common.cbclib.IDType;
import org.dgpe.codice.common.cbclib.MeasureType;

/**
 * Mapper para convertir {@link ContractModificationType} (CÓDICE) a {@link ContractModification}
 * (JPA).
 *
 * <p>Contrato:
 *
 * <ul>
 *   <li>Vincula cada modificación con su {@link ContractFolderStatus} padre.
 *   <li>Los campos string se truncan según {@link Constantes}.
 *   <li>Los subnodos opcionales (monetarios y medidas) se mapean solo si existen.
 * </ul>
 */
public final class MapperContractModification {

  /** Constructor privado para evitar instanciación. */
  private MapperContractModification() {
    // Utility class.
  }

  /**
   * Convierte una lista de modificaciones CÓDICE a lista de entidades {@link ContractModification}.
   *
   * @param contractFolderStatus entidad padre (no nula).
   * @param contractModificationTypeList lista de modificaciones CÓDICE (no nula; puede ser vacía).
   * @return lista inmutable de modificaciones mapeadas.
   * @throws NullPointerException si algún parámetro obligatorio es {@code null}.
   */
  public static List<ContractModification> getListContractModificationFromType(
      ContractFolderStatus contractFolderStatus,
      List<ContractModificationType> contractModificationTypeList) {

    Objects.requireNonNull(contractFolderStatus, "contractFolderStatus no puede ser null");
    Objects.requireNonNull(
        contractModificationTypeList, "contractModificationTypeList no puede ser null");

    return contractModificationTypeList.stream()
        .filter(Objects::nonNull)
        .map(type -> mapContractModification(contractFolderStatus, type))
        .toList();
  }

  private static ContractModification mapContractModification(
      ContractFolderStatus contractFolderStatus, ContractModificationType type) {

    ContractModification entity = new ContractModification();
    entity.setContractFolderStatus(contractFolderStatus);

    mapIdentifiers(entity, type);
    mapIssueDate(entity, type);
    mapNote(entity, type);
    mapLegalMonetaryTotals(entity, type);
    mapMeasures(entity, type);

    return entity;
  }

  private static void mapIdentifiers(ContractModification entity, ContractModificationType type) {
    setLimited(entity::setContractId, safeValue(type.getContractID(), ContractIDType::getValue));

    setLimited(entity::setContractModificationId, safeValue(type.getID(), IDType::getValue));
  }

  private static void mapIssueDate(ContractModification entity, ContractModificationType type) {
    if (type.getIssueDate() == null) {
      return;
    }
    if (type.getIssueDate().getValue() == null) {
      return;
    }
    entity.setIssueDate(
        GregorianCalendarHelper.getDateFromXMLGregorianCalendar(type.getIssueDate().getValue()));
  }

  private static void mapNote(ContractModification entity, ContractModificationType type) {
    // Según tu contexto: listas no nulas (al menos vacías). Si note fuese null, el helper debería
    // tolerarlo.
    String note = MapperStringFromList.getStringFromListNoteType(type.getNote());
    entity.setNote(StringHelper.eliminarCaracteres(note));
  }

  private static void mapLegalMonetaryTotals(
      ContractModification entity, ContractModificationType type) {
    mapTaxExclusiveAmount(
        type.getContractModificationLegalMonetaryTotal(),
        entity::setContractModificationTaxExclusiveAmount);

    mapTaxExclusiveAmount(type.getFinalLegalMonetaryTotal(), entity::setFinalTaxExclusiveAmount);
  }

  private static void mapTaxExclusiveAmount(
      LegalMonetaryTotalType legalMonetaryTotalType, java.util.function.Consumer<Double> setter) {

    if (legalMonetaryTotalType == null) {
      return;
    }
    LegalMonetaryTotal total =
        LegalMonetaryTotalMapper.getLegalMonetaryTotalFromType(legalMonetaryTotalType);
    setter.accept(total.getTaxExclusiveAmount());
  }

  private static void mapMeasures(ContractModification entity, ContractModificationType type) {
    MeasureType measureType = new MeasureType();
    if (type.getContractModificationDurationMeasure() != null) {
      measureType.setUnitCode(type.getContractModificationDurationMeasure().getUnitCode());
      measureType.setValue(type.getContractModificationDurationMeasure().getValue());
      mapMeasure(
          measureType,
          entity::setContractModificationMeasureUnitCode,
          entity::setContractModificationMeasureValue);
    }
    if (type.getFinalDurationMeasure() != null) {
      measureType.setUnitCode(type.getFinalDurationMeasure().getUnitCode());
      measureType.setValue(type.getFinalDurationMeasure().getValue());
      mapMeasure(measureType, entity::setFinalMeauseUnitCode, entity::setFinalMeasureValue);
    }
  }

  private static void mapMeasure(
      MeasureType measureType,
      java.util.function.Consumer<String> unitCodeSetter,
      java.util.function.Consumer<java.math.BigDecimal> valueSetter) {

    if (measureType == null) {
      return;
    }
    Measure measure = MeassureMapper.getMeasure(measureType);
    unitCodeSetter.accept(measure.getUnitCode());
    valueSetter.accept(measure.getValue());
  }

  private static <T> String safeValue(T obj, java.util.function.Function<T, String> mapper) {
    if (obj == null) {
      return null;
    }
    return mapper.apply(obj);
  }

  private static void setLimited(Consumer<String> setter, String value) {

    if (value == null) {
      return;
    }
    setter.accept(StringHelper.limit(value, TamanoCampos.TAMANO_50));
  }
}
