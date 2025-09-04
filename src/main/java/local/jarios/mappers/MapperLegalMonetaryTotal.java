package local.jarios.mappers;

import local.jarios.entity.placsp.ContractModification;
import local.jarios.entity.placsp.LegalMonetaryTotal;
import local.jarios.entity.placsp.TenderedProject;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.LegalMonetaryTotalType;

import java.util.Optional;

/**
 * Mapper encargado de transformar objetos {@link LegalMonetaryTotalType} del modelo Codice en la
 * entidad interna {@link LegalMonetaryTotal} utilizada en el proyecto.
 * <p>
 * Esta clase permite asociar un total monetario legal con sus entidades relacionadas como proyectos
 * licitados y modificaciones de contrato, además de mapear las cantidades monetarias relevantes
 * (pagable, sin impuestos, con impuestos).
 * </p>
 *
 * <p><b>Autor:</b> Juan Antonio</p>
 * <p><b>Fecha:</b> 06/07/2024</p>
 * <p><b>Equipo:</b> Juan Antonio</p>
 */
@Slf4j
public final class MapperLegalMonetaryTotal {

  private MapperLegalMonetaryTotal() {
    // Constructor privado para evitar instanciación
  }

  /**
   * Construye una entidad {@link LegalMonetaryTotal} a partir del objeto
   * {@link LegalMonetaryTotalType} recibido y las entidades relacionadas indicadas.
   *
   * @param tenderedProject        Proyecto licitado asociado al total monetario.
   * @param contractModificacion   Modificación de contrato relacionada (primera).
   * @param contractModification2  Modificación de contrato relacionada (final).
   * @param legalMonetaryTotalType Objeto Codice con datos de totales monetarios legales.
   * @return {@link LegalMonetaryTotal} mapeado con los datos y asociaciones correspondientes.
   */
  public static LegalMonetaryTotal getLegalMonetaryTotalFromType(
      TenderedProject tenderedProject,
      ContractModification contractModificacion,
      ContractModification contractModification2,
      LegalMonetaryTotalType legalMonetaryTotalType) {

    LegalMonetaryTotal legalMonetaryTotal = new LegalMonetaryTotal();
    legalMonetaryTotal.setAwardedTenderedProject(tenderedProject);
    legalMonetaryTotal.setContractModificationLegalMonetaryTotal(contractModificacion);
    legalMonetaryTotal.setContractModificationFinalLegalMonetaryTotal(contractModification2);

    Optional.ofNullable(legalMonetaryTotalType.getPayableAmount())
        .map(amount -> amount.getValue().doubleValue())
        .ifPresent(legalMonetaryTotal::setPayableAmount);

    Optional.ofNullable(legalMonetaryTotalType.getTaxExclusiveAmount())
        .map(amount -> amount.getValue().doubleValue())
        .ifPresent(legalMonetaryTotal::setTaxExclusiveAmount);

    Optional.ofNullable(legalMonetaryTotalType.getTaxInclusiveAmount())
        .map(amount -> amount.getValue().doubleValue())
        .ifPresent(legalMonetaryTotal::setTaxInclusiveAmount);

    return legalMonetaryTotal;
  }
}
