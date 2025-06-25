package local.jarios.mappers;

import local.jarios.entity.placsp.AgentParty;
import local.jarios.entity.placsp.Party;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
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
public final class MapperAgentParty {

    private MapperAgentParty() { }

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
        String partyName = MapperStringFromList.getStringFromListPartyNameType(partyType.getPartyName());
        agentParty.setPartyName(
                ComunHelper.limitarRegistro(partyName, Constantes.TAMANO_MAXIMO_CAMPO_250));

        // Party Identification
        agentParty.setPartyIdentification(
                MapperPartyIdentification.getPartyIdentification(
                        null,
                        agentParty,
                        null,
                        partyType.getPartyIdentification()));

        log.debug(agentParty.toString());

        //
        return agentParty;
    }
}
