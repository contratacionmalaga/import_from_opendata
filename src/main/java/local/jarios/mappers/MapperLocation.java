package local.jarios.mappers;

import local.jarios.entity.placsp.Location;
import local.jarios.entity.placsp.Party;
import local.jarios.entity.placsp.ProcurementProject;
import local.jarios.entity.placsp.WinningParty;
import local.jarios.helpers.ComunHelper;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.LocationType;

import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperLocation {

    private MapperLocation() { }

    public static Location getLocation(
            ProcurementProject procurementProject,
            Party party,
            WinningParty winningParty,
            LocationType locationType) {

        if (locationType == null) {
            return null;
        }

        //
        Location location = new Location();
        location.setProcurementProject(procurementProject);
        location.setParty(party);
        location.setWinningParty(winningParty);

        Optional.ofNullable(locationType.getCountrySubentityCode())
                .map(code -> ComunHelper.limitarRegistro(code.getValue(), Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(location::setCountrySubentityCode);

        Optional.ofNullable(locationType.getCountrySubentity())
                .map(sub -> ComunHelper.limitarRegistro(sub.getValue(), Constantes.TAMANO_MAXIMO_CAMPO_250))
                .ifPresent(location::setCountrySubentity);

        Optional.ofNullable(locationType.getAddress())
                .map(addr -> MapperAddress.getAddress(null, location, null, addr))
                .ifPresent(location::setAddress);

        return location;
    }
}
