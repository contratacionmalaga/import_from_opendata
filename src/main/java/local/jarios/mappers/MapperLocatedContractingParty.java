package local.jarios.mappers;

import ext.place.codice.common.caclib.LocatedContractingPartyType;
import local.jarios.entity.placsp.ContractFolderStatus;
import local.jarios.entity.placsp.LocatedContractingParty;
import local.jarios.entity.placsp.PreliminaryMarketConsultationStatus;
import local.jarios.helpers.ComunHelper;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperLocatedContractingParty {

    private MapperLocatedContractingParty() { }

    public static LocatedContractingParty getLocatedContractingPartyFromType(
            ContractFolderStatus contractFolderStatus,
            PreliminaryMarketConsultationStatus preliminaryMarketConsultationStatus,
            LocatedContractingPartyType locatedContractingPartyType) {

        if (locatedContractingPartyType == null) {
            return null;
        }

        // LocatedContractingParty
        LocatedContractingParty locatedContractingParty = new LocatedContractingParty();
        locatedContractingParty.setContractFolderStatus(contractFolderStatus);
        locatedContractingParty.setPreliminaryMarketConsultationStatus(preliminaryMarketConsultationStatus);

        Optional.ofNullable(locatedContractingPartyType.getContractingPartyTypeCode())
                .map(code -> ComunHelper.limitarRegistro(
                        code.getValue(),
                        Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(locatedContractingParty::setContractingPartyTypeCode);

        Optional.ofNullable(locatedContractingPartyType.getBuyerProfileURIID())
                .map(uri -> ComunHelper.limitarRegistro(
                        uri.getValue(),
                        Constantes.TAMANO_MAXIMO_CAMPO_2500))
                .ifPresent(locatedContractingParty::setBuyerProfileUriId);

        locatedContractingParty.setParty(
                MapperParty.getPartyFromType(
                        locatedContractingParty,
                        locatedContractingPartyType.getParty()));

        return locatedContractingParty;
    }
}

