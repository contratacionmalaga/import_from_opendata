package local.jarios.mappers.codice;

import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.AwardingCriteria;
import local.jarios.entity.codice.AwardingTerms;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AwardingCriteriaType;
import org.dgpe.codice.common.cbclib.AwardingCriteriaSubTypeCodeType;
import org.dgpe.codice.common.cbclib.AwardingCriteriaTypeCodeType;

import java.util.List;
import java.util.Optional;

/**
 * Mapper para transformar objetos {@link AwardingCriteriaType} del modelo Codice a entidades
 * internas {@link AwardingCriteria}.
 *
 * <p>Proporciona métodos para convertir listas de tipos Codice
 * a listas de entidades de dominio.</p>
 *
 * <b>Autor:</b> Juan Antonio
 * <b>Fecha:</b> 06/07/2024
 * <b>Equipo:</b> Juan Antonio
 */
@Slf4j
public final class MapperAwardingCriteria {

  private MapperAwardingCriteria() {
    // Constructor privado para evitar instanciación
  }

  /**
   * Convierte una lista de objetos {@link AwardingCriteriaType} en una lista de entidades
   * {@link AwardingCriteria}, asignadas al {@link AwardingTerms} dado.
   *
   * @param awardingTerms            Entidad padre {@link AwardingTerms} para asignar a cada
   *                                 {@link AwardingCriteria}
   * @param listAwardingCriteriaType Lista de objetos {@link AwardingCriteriaType} a transformar
   * @return Lista de entidades {@link AwardingCriteria} mapeadas
   */
  public static List<AwardingCriteria> getListAwardingCriteria(
      AwardingTerms awardingTerms,
      List<AwardingCriteriaType> listAwardingCriteriaType) {

    // Java 16+. Si usas Java 8, reemplaza con .collect(Collectors.toList())
    return listAwardingCriteriaType.stream()
        .map(type -> getAwardingCriteria(awardingTerms, type))
        .toList();
  }

  /**
   * Transforma un objeto {@link AwardingCriteriaType} en una entidad {@link AwardingCriteria}.
   *
   * @param awardingTerms        Entidad padre {@link AwardingTerms} para asignar a la entidad
   *                             resultado
   * @param awardingCriteriaType Objeto {@link AwardingCriteriaType} a transformar
   * @return Entidad {@link AwardingCriteria} resultante
   */
  private static AwardingCriteria getAwardingCriteria(
      AwardingTerms awardingTerms,
      AwardingCriteriaType awardingCriteriaType) {

    AwardingCriteria awardingCriteria = new AwardingCriteria();

    awardingCriteria.setAwardingTerms(awardingTerms);

    // Mapeo seguro y limitado de AwardingCriteriaTypeCode
    Optional.ofNullable(awardingCriteriaType.getAwardingCriteriaTypeCode())
        .map(AwardingCriteriaTypeCodeType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(awardingCriteria::setAwardingCriteriaTypeCode);

    // Mapeo seguro y limitado de AwardingCriteriaSubTypeCode
    Optional.ofNullable(awardingCriteriaType.getAwardingCriteriaSubTypeCode())
        .map(AwardingCriteriaSubTypeCodeType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(awardingCriteria::setAwardingCriteriaSubTypeCode);

    // Mapeo del peso numérico
    Optional.ofNullable(awardingCriteriaType.getWeightNumeric())
        .map(n -> n.getValue().doubleValue())
        .ifPresent(awardingCriteria::setWeightNumeric);

    // Mapeo de descripción usando helper
    awardingCriteria.setDescription(
        MapperStringFromList.getStringFromListDescriptionType(
            awardingCriteriaType.getDescription()));

    // Mapeo de notas usando helper
    awardingCriteria.setNote(
        MapperStringFromList.getStringFromListNoteType(
            awardingCriteriaType.getNote()));

    return awardingCriteria;
  }
}
