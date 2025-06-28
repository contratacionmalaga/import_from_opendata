package local.jarios.mappers;

import local.jarios.entity.placsp.AuctionTerms;
import local.jarios.entity.placsp.TenderingProcess;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AuctionTermsType;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperAuctionTerms {

    private MapperAuctionTerms() {
    }

    public static AuctionTerms getAuctionTerms(
            TenderingProcess tenderingProcess,
            AuctionTermsType auctionTermsType) {

        //
        AuctionTerms auctionTerms = new AuctionTerms();

        //
        auctionTerms.setTenderingProcess(tenderingProcess);

        //
        auctionTerms.setAuctionConstraintIndicator(
                auctionTermsType.getAuctionConstraintIndicator().isValue());

        //
        return auctionTerms;
    }
}
