package local.jarios.mappers;

import local.jarios.entity.placsp.TenderRecipientParty;
import local.jarios.entity.placsp.TenderingTerms;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.PartyType;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperTenderRecipientParty {

    private MapperTenderRecipientParty() { }

    public static TenderRecipientParty getTenderRecipientParty(
            TenderingTerms tenderingTerms,
            PartyType partyType) {

        //
        TenderRecipientParty tenderRecipientParty = new TenderRecipientParty();
        tenderRecipientParty.setTenderingTerms(tenderingTerms);
        tenderRecipientParty.setEndpointId(partyType.getEndpointID().getValue());

        //
        return tenderRecipientParty;
    }
}
