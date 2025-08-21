package local.jarios.mappers;

import local.jarios.common.util.Constantes;
import local.jarios.entity.placsp.LocatedContractingParty;
import local.jarios.entity.placsp.Party;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.PartyType;
import org.dgpe.codice.common.cbclib.WebsiteURIType;

import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperParty {

  private MapperParty() {
  }

  public static Party getPartyFromType(
      LocatedContractingParty locatedContractingParty,
      PartyType partyType) {

    //
    Party party = new Party();
    party.setLocatedContractingParty(locatedContractingParty);

    Optional.ofNullable(partyType.getWebsiteURI())
        .map(WebsiteURIType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_2500))
        .ifPresent(party::setWebSiteUri);

    party.setPartyName(
        ComunHelper.limitarRegistro(
            MapperStringFromList.getStringFromListPartyNameType(partyType.getPartyName()),
            Constantes.TAMANO_MAXIMO_CAMPO_500));

    Optional.ofNullable(partyType.getPostalAddress())
        .ifPresent(addr -> party.setPostalAddress(
            MapperAddress.getAddress(party, null, null, addr)));

    Optional.ofNullable(partyType.getPhysicalLocation())
        .ifPresent(loc -> party.setPhysicalLocation(
            MapperLocation.getLocation(null, party, null, loc)));

    Optional.ofNullable(partyType.getContact())
        .ifPresent(contact -> party.setContact(
            MapperContact.getContact(party, null, contact)));

    Optional.ofNullable(partyType.getAgentParty())
        .ifPresent(agentParty -> party.setAgentParty(
            MapperAgentParty.getAgentParty(party, agentParty)));

    party.setPartyIdentification(
        MapperPartyIdentification.getPartyIdentification(
            party,
            null,
            null,
            partyType.getPartyIdentification()));

    return party;
  }
}
