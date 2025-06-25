package local.jarios.mappers;

import local.jarios.entity.placsp.Address;
import local.jarios.entity.placsp.Location;
import local.jarios.entity.placsp.Party;
import local.jarios.entity.placsp.WinningParty;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AddressType;
import org.dgpe.codice.common.cbclib.CityNameType;
import org.dgpe.codice.common.cbclib.PostalZoneType;

import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperAddress {

    private MapperAddress() { }

    public static Address getAddress(
            Party party,
            Location location,
            WinningParty winningParty,
            AddressType addressType) {

        //
        Address address = new Address();

        //
        address.setParty(party);
        address.setLocation(location);
        address.setWinningParty(winningParty);

        // Campos opcionales con sanitización
        Optional.ofNullable(addressType.getCityName())
                .map(CityNameType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_250))
                .ifPresent(address::setCityName);

        //
        Optional.ofNullable(addressType.getPostalZone())
                .map(PostalZoneType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(address::setPostalZone);

        // AddressLine nunca es null
        String addressLine = MapperStringFromList.getStringFromListAddressLineType(addressType.getAddressLine());
        address.setAddressLine(
                ComunHelper.limitarRegistro(addressLine, Constantes.TAMANO_MAXIMO_CAMPO_2500));

        // Country opcional
        Optional.ofNullable(addressType.getCountry())
                .map(countryType -> MapperCountry.getCountry(address, countryType))
                .ifPresent(address::setCountry);


        log.debug(address.toString());
        //
        return address;
    }
}
