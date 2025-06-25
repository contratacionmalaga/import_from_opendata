package local.jarios.mappers;

import local.jarios.entity.placsp.Address;
import local.jarios.entity.placsp.Country;
import local.jarios.helpers.ComunHelper;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.CountryType;

import java.util.Optional;

/**
 * Description: Juan Antonio
 * Author: juan
 * Date: 06/07/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperCountry {

    private MapperCountry() { }

    public static Country getCountry(
            Address address,
            CountryType countryType) {

        //
        Country country = new Country();
        country.setAddress(address);

        Optional.ofNullable(countryType.getName())
                .map(name -> ComunHelper.limitarRegistro(name.getValue(), Constantes.TAMANO_MAXIMO_CAMPO_250))
                .ifPresent(country::setName);

        Optional.ofNullable(countryType.getIdentificationCode())
                .map(code -> ComunHelper.limitarRegistro(code.getValue(), Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(country::setIdentificationCode);

        return country;
    }
}
