package local.jarios.mappers;

import local.jarios.entity.placsp.AgentParty;
import local.jarios.entity.placsp.Party;
import local.jarios.entity.placsp.PartyIdentification;
import local.jarios.entity.placsp.WinningParty;
import local.jarios.helpers.ComunHelper;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.PartyIdentificationType;

import java.util.List;
import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperPartyIdentification {

    private MapperPartyIdentification() { }

    public static PartyIdentification getPartyIdentification(
            Party party,
            AgentParty agentParty,
            WinningParty winningParty,
            List<PartyIdentificationType> partyIdentificationTypeList) {

        //
        PartyIdentification partyIdentification = new PartyIdentification();
        partyIdentification.setParty(party);
        partyIdentification.setAgentParty(agentParty);
        partyIdentification.setWinningParty(winningParty);

        for (PartyIdentificationType partyIdentificationType : partyIdentificationTypeList) {
            Optional.ofNullable(partyIdentificationType.getID())
                    .ifPresent(id -> {
                        String partyIdentificationId = id.getValue();
                        Optional.ofNullable(id.getSchemeName())
                                .ifPresent(schemeName -> {
                                    String limitedId = ComunHelper.limitarRegistro(
                                            partyIdentificationId,
                                            Constantes.TAMANO_MAXIMO_CAMPO_50);
                                    switch (schemeName) {
                                        case Constantes.DIR3 -> partyIdentification.setDir3(limitedId);
                                        case Constantes.IDPLATAFORMA -> partyIdentification.setIdPlataforma(limitedId);
                                        case Constantes.IDOCPLAT -> partyIdentification.setIdOcPlat(limitedId);
                                        case Constantes.NIF -> partyIdentification.setNif(limitedId);
                                        case Constantes.OTROS -> {
                                            partyIdentification.setOtros(limitedId);
                                            partyIdentification.setNif(limitedId);
                                        }
                                    }
                                });
                    });
        }

        return partyIdentification;
    }
}
