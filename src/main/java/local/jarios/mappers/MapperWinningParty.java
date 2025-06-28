package local.jarios.mappers;

import local.jarios.entity.placsp.TenderResult;
import local.jarios.entity.placsp.WinningParty;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.PartyType;

import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperWinningParty {

    private MapperWinningParty() { }

    public static WinningParty getWinningParty(
            TenderResult tenderResult,
            PartyType partyType) {

        //
        WinningParty winningParty = new WinningParty();
        winningParty.setTenderResult(tenderResult);

        winningParty.setPartyName(
                ComunHelper.limitarRegistro(
                        MapperStringFromList.getStringFromListPartyNameType(partyType.getPartyName()),
                        Constantes.TAMANO_MAXIMO_CAMPO_250));

        //
        log.debug(winningParty.toString());

        Optional.ofNullable(partyType.getPostalAddress())
                .ifPresent(postalAddress -> winningParty.setPostalAddress(
                        MapperAddress.getAddress(null, null, winningParty, postalAddress)));

        Optional.ofNullable(partyType.getPhysicalLocation())
                .ifPresent(physicalLocation -> winningParty.setPhysicalLocation(
                        MapperLocation.getLocation(null, null, winningParty, physicalLocation)));

        Optional.ofNullable(partyType.getContact())
                .ifPresent(contact -> winningParty.setContact(
                        MapperContact.getContact(null, winningParty, contact)));

        // partyIdentification se asume que puede ser null, pero no chequeado antes
        winningParty.setPartyIdentification(
                MapperPartyIdentification.getPartyIdentification(
                        null, null, winningParty, partyType.getPartyIdentification()));

        //
        return winningParty;
    }
}
