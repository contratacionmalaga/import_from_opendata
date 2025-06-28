package local.jarios.mappers;

import local.jarios.entity.placsp.ContractModification;
import local.jarios.entity.placsp.LegalMonetaryTotal;
import local.jarios.entity.placsp.TenderedProject;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.LegalMonetaryTotalType;

import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperLegalMonetaryTotal {

    private MapperLegalMonetaryTotal() { }

    public static LegalMonetaryTotal getLegalMonetaryTotalFromType(
            TenderedProject tenderedProject,
            ContractModification contractModificacion,
            ContractModification contractModification2,
            LegalMonetaryTotalType legalMonetaryTotalType) {

        //
        LegalMonetaryTotal legalMonetaryTotal = new LegalMonetaryTotal();
        legalMonetaryTotal.setAwardedTenderedProject(tenderedProject);
        legalMonetaryTotal.setContractModificationLegalMonetaryTotal(contractModificacion);
        legalMonetaryTotal.setContractModificationFinalLegalMonetaryTotal(contractModification2);

        Optional.ofNullable(legalMonetaryTotalType.getPayableAmount())
                .map(amount -> amount.getValue().doubleValue())
                .ifPresent(legalMonetaryTotal::setPayableAmount);

        Optional.ofNullable(legalMonetaryTotalType.getTaxExclusiveAmount())
                .map(amount -> amount.getValue().doubleValue())
                .ifPresent(legalMonetaryTotal::setTaxExclusiveAmount);

        Optional.ofNullable(legalMonetaryTotalType.getTaxInclusiveAmount())
                .map(amount -> amount.getValue().doubleValue())
                .ifPresent(legalMonetaryTotal::setTaxInclusiveAmount);

        return legalMonetaryTotal;
    }
}
