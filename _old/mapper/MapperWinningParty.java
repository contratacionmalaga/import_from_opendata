package local.jarios.mappers.codice;

import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.TenderResult;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.PartyType;

import java.util.Optional;

/**
 * Clase utilitaria para mapear objetos {@link PartyType} a entidades de negocio
 * {@link WinningParty}.
 *
 * <p>Esta clase proporciona un método estático para construir una instancia de {@link WinningParty} a
 * partir de los datos contenidos en un {@link PartyType} recibido.
 * El mapeo incluye campos como nombre, dirección postal, ubicación física, contacto e
 * identificación de la parte ganadora.
 * Todos los campos anidados son mapeados usando clases auxiliares específicas (como
 * {@link MapperAddress}, {@link MapperLocation}, etc.) y el tamaño de los campos de texto está
 * limitado para cumplir con las restricciones del sistema.
 * Esta clase es estática y no instanciable.</p>
 *
 * <b>Autor:</b> Juan Antonio
 * <b>Fecha:</b> 06/07/2024
 * <b>Equipo:</b> Juan Antonio
 */
@Slf4j
public final class MapperWinningParty {

  private MapperWinningParty() {
  }

  /**
   * Construye un objeto {@link WinningParty} mapeando los datos del {@link PartyType} recibido y
   * vinculándolo con un {@link TenderResult}.
   *
   * @param tenderResult entidad {@link TenderResult} asociada al ganador.
   * @param partyType    objeto {@link PartyType} que contiene los datos a mapear.
   * @return instancia de {@link WinningParty} con los datos mapeados.
   */
  public static WinningParty getWinningParty(
      TenderResult tenderResult,
      PartyType partyType) {

    WinningParty winningParty = new WinningParty();
    winningParty.setTenderResult(tenderResult);

    winningParty.setPartyName(
        ComunHelper.limitarRegistro(
            MapperStringFromList.getStringFromListPartyNameType(partyType.getPartyName()),
            Constantes.TAMANO_MAXIMO_CAMPO_500));

    Optional.ofNullable(partyType.getPostalAddress())
        .ifPresent(postalAddress -> winningParty.setPostalAddress(
            MapperAddress.getAddress(null, null, winningParty, postalAddress)));

    Optional.ofNullable(partyType.getPhysicalLocation())
        .ifPresent(physicalLocation -> winningParty.setPhysicalLocation(
            MapperLocation.getLocation(
                null,
                null, winningParty,
                physicalLocation)));

    Optional.ofNullable(partyType.getContact())
        .ifPresent(contact -> winningParty.setContact(
            MapperContact.getContact(null, winningParty, contact)));

    // La identificación de la parte puede ser null, no se realiza chequeo previo.
    winningParty.setPartyIdentification(
        MapperPartyIdentification.getPartyIdentification(
            null, null, winningParty, partyType.getPartyIdentification()));

    return winningParty;
  }
}
