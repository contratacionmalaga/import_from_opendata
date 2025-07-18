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
 * Mapper para transformar objetos {@link AddressType} del modelo Codice
 * a entidades internas {@link Address}.
 *
 * <p>Permite construir una instancia de {@link Address} a partir
 * de los objetos {@link Party}, {@link Location}, {@link WinningParty}
 * y el tipo {@link AddressType} proporcionado.</p>
 *
 * <p>Realiza sanitización y limitación de tamaño de campos para evitar
 * errores por datos demasiado extensos.</p>
 *
 * @author juan
 */
@Slf4j
public final class MapperAddress {

    private MapperAddress() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Construye un objeto {@link Address} a partir de los datos proporcionados.
     *
     * @param party Objeto {@link Party} al que pertenece la dirección, puede ser null.
     * @param location Objeto {@link Location} asociado, puede ser null.
     * @param winningParty Objeto {@link WinningParty} asociado, puede ser null.
     * @param addressType Objeto {@link AddressType} fuente de datos para el mapeo.
     * @return Instancia de {@link Address} construida.
     */
    public static Address getAddress(
            Party party,
            Location location,
            WinningParty winningParty,
            AddressType addressType) {

        Address address = new Address();

        address.setParty(party);
        address.setLocation(location);
        address.setWinningParty(winningParty);

        Optional.ofNullable(addressType.getCityName())
                .map(CityNameType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_500))
                .ifPresent(address::setCityName);

        Optional.ofNullable(addressType.getPostalZone())
                .map(PostalZoneType::getValue)
                .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
                .ifPresent(address::setPostalZone);

        String addressLine = MapperStringFromList.getStringFromListAddressLineType(addressType.getAddressLine());
        address.setAddressLine(
                ComunHelper.limitarRegistro(addressLine, Constantes.TAMANO_MAXIMO_CAMPO_2500));

        Optional.ofNullable(addressType.getCountry())
                .map(countryType -> MapperCountry.getCountry(address, countryType))
                .ifPresent(address::setCountry);

        return address;
    }
}
