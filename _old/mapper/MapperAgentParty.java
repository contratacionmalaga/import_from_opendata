package local.jarios.mappers.codice;

import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.AgentParty;
import local.jarios.entity.codice.Party;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.PartyType;
import org.dgpe.codice.common.cbclib.WebsiteURIType;

import java.util.Optional;

/**
 * Description: Mapeador de AgentParty.
 * <b>Autor:</b> Juan Antonio
 * <b>Fecha:</b> 06/07/2024
 * <b>Equipo:</b> Juan Antonio
 */
@Slf4j
public final class MapperAgentParty {

  private MapperAgentParty() {
  }

  public static AgentParty getAgentParty(
      Party party,
      PartyType partyType) {

    //
    AgentParty agentParty = new AgentParty();

    //
    agentParty.setParty(party);

    // Website URI (si no es null)
    Optional.ofNullable(partyType.getWebsiteURI())
        .map(WebsiteURIType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_2500))
        .ifPresent(agentParty::setWebSiteUri);

    // Party Name (siempre se procesa)
    String partyName = MapperStringFromList.getStringFromListPartyNameType(
        partyType.getPartyName());
    agentParty.setPartyName(
        ComunHelper.limitarRegistro(partyName, Constantes.TAMANO_MAXIMO_CAMPO_500));

    // Party Identification
    agentParty.setPartyIdentification(
        MapperPartyIdentification.getPartyIdentification(
            null,
            agentParty,
            null,
            partyType.getPartyIdentification()));

    //
    return agentParty;
  }
}
