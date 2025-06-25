package local.jarios.mappers;

import local.jarios.entity.placsp.AwardingTerms;
import local.jarios.entity.placsp.TenderingTerms;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AwardingTermsType;

import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperAwardingTerms {

    private MapperAwardingTerms() {
    }

    public static AwardingTerms getAwardingTerms(
            TenderingTerms tenderingTerms,
            AwardingTermsType awardingTermsType) {

        //
        AwardingTerms awardingTerms = new AwardingTerms();

        //
        awardingTerms.setTenderingTerms(tenderingTerms);

        //
        Optional.ofNullable(awardingTermsType.getAwardingCriteria())
                .filter(list -> !list.isEmpty())
                .map(list -> MapperAwardingCriteria.getListAwardingCriteria(awardingTerms, list))
                .ifPresent(awardingTerms::setListAwardingCriteria);

        log.debug(awardingTerms.toString());
        //
        return awardingTerms;
    }
}
