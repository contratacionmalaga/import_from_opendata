package local.jarios.mappers;

import local.jarios.entity.placsp.FinancialGuarantee;
import local.jarios.entity.placsp.TenderingTerms;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.FinancialGuaranteeType;
import org.dgpe.codice.common.cbclib.GuaranteeTypeCodeType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperFinancialGuarantee {

    private MapperFinancialGuarantee() { }

    public static List<FinancialGuarantee> getListFinancialGuarantee (
            TenderingTerms tenderingTerms,
            List<FinancialGuaranteeType> listFinancialGuaranteeType) {

        //
        return Optional.ofNullable(listFinancialGuaranteeType)
                .map(list -> list.stream()
                        .map(financialGuaranteeType -> getFinancialGuarantee(tenderingTerms, financialGuaranteeType))
                        .toList())
                .orElse(Collections.emptyList());
    }

    private static FinancialGuarantee getFinancialGuarantee (
            TenderingTerms tenderingTerms,
            FinancialGuaranteeType financialGuaranteeType) {

        //
        var financialGuarantee = new FinancialGuarantee();
        financialGuarantee.setTenderingTerms(tenderingTerms);

        Optional.ofNullable(financialGuaranteeType.getGuaranteeTypeCode())
                .map(GuaranteeTypeCodeType::getValue)
                .ifPresent(financialGuarantee::setGuaranteeTypeCode);

        Optional.ofNullable(financialGuaranteeType.getAmountRate())
                .map(rate -> rate.getValue().doubleValue())
                .ifPresent(financialGuarantee::setAmountRate);

        Optional.ofNullable(financialGuaranteeType.getLiabilityAmount())
                .map(amount -> amount.getValue().doubleValue())
                .ifPresent(financialGuarantee::setLiabilityAmount);

        log.debug(financialGuarantee.toString());
        return financialGuarantee;
    }
}
