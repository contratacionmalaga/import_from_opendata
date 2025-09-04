package local.jarios.mappers;

import local.jarios.entity.placsp.TenderRecipientParty;
import local.jarios.entity.placsp.TenderingTerms;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.PartyType;

/**
 * Clase utilitaria para mapear objetos del modelo Codice {@link PartyType} a la entidad persistente
 * {@link TenderRecipientParty}.
 * <p>
 * Esta clase contiene métodos estáticos para crear instancias de {@link TenderRecipientParty}
 * asociadas a {@link TenderingTerms} a partir de objetos de tipo {@link PartyType}.
 * </p>
 * <p>
 * Clase final e ininstanciable.
 * </p>
 *
 * <b>Autor:</b> Juan Antonio
 * <b>Fecha:</b> 06/07/2024
 * <b>Equipo:</b> Juan Antonio
 */
@Slf4j
public final class MapperTenderRecipientParty {

  private MapperTenderRecipientParty() {
  }

  /**
   * Crea una instancia de {@link TenderRecipientParty} a partir de un objeto {@link PartyType} y la
   * asocia a un objeto {@link TenderingTerms}.
   *
   * @param tenderingTerms entidad padre a la que se asociará el {@link TenderRecipientParty}.
   * @param partyType      objeto fuente con datos para el mapeo.
   * @return instancia de {@link TenderRecipientParty} con los datos mapeados.
   */
  public static TenderRecipientParty getTenderRecipientParty(
      TenderingTerms tenderingTerms,
      PartyType partyType) {

    TenderRecipientParty tenderRecipientParty = new TenderRecipientParty();
    tenderRecipientParty.setTenderingTerms(tenderingTerms);

    if (partyType.getEndpointID() != null) {
      tenderRecipientParty.setEndpointId(partyType.getEndpointID().getValue());
    } else {
      log.warn("El PartyType recibido no tiene EndpointID definido.");
    }

    return tenderRecipientParty;
  }
}
