package local.jarios.codice;

import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.LegalMonetaryTotalType;

import java.util.Optional;

/**
 * Mapper encargado de transformar objetos {@link LegalMonetaryTotalType} del modelo Codice en la
 * entidad interna {@link LegalMonetaryTotal} utilizada en el proyecto.
 */
@Slf4j
public final class LegalMonetaryTotalMapper {

  private LegalMonetaryTotalMapper() {
    // Constructor privado para evitar instanciación
  }

  /**
   * Construye una entidad {@link LegalMonetaryTotal} a partir del objeto
   * {@link LegalMonetaryTotalType} recibido y las entidades relacionadas indicadas.
   *
   * @param legalMonetaryTotalType Objeto Codice con datos de totales monetarios legales.
   * @return {@link LegalMonetaryTotal} mapeado con los datos y asociaciones correspondientes.
   */
  public static LegalMonetaryTotal getLegalMonetaryTotalFromType(
      LegalMonetaryTotalType legalMonetaryTotalType) {

    LegalMonetaryTotal legalMonetaryTotal = new LegalMonetaryTotal();

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
