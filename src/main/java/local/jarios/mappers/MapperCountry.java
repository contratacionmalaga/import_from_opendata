package local.jarios.mappers;

import local.jarios.common.util.Constantes;
import local.jarios.entity.placsp.Address;
import local.jarios.entity.placsp.Country;
import local.jarios.helpers.ComunHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.CountryType;

import java.util.Optional;

/**
 * Mapper para convertir objetos {@link CountryType} del modelo Codice a la entidad interna
 * {@link Country} usada en el proyecto.
 * <p>
 * Proporciona métodos para transformar datos de país asociados a una dirección en objetos
 * persistibles.
 * <p>
 * Se aplican límites de tamaño a los campos para garantizar la compatibilidad con la base de datos
 * y evitar errores por datos demasiado largos.
 *
 * <p><b>Autor:</b> Juan Antonio</p>
 * <p><b>Fecha:</b> 06/07/2024</p>
 * <p><b>Equipo:</b> Juan Antonio</p>
 */
@Slf4j
public final class MapperCountry {

  private MapperCountry() {
    // Constructor privado para evitar instanciación
  }

  /**
   * Convierte un objeto {@link CountryType} en una entidad {@link Country} vinculada a una
   * {@link Address} dada.
   *
   * @param address     Entidad {@link Address} a la que se asocia el país.
   * @param countryType Objeto {@link CountryType} que contiene los datos del país.
   * @return Objeto {@link Country} construido a partir de {@code countryType}.
   */
  public static Country getCountry(Address address, CountryType countryType) {
    Country country = new Country();
    country.setAddress(address);

    Optional.ofNullable(countryType.getName())
        .map(name -> ComunHelper.limitarRegistro(name.getValue(),
                                                 Constantes.TAMANO_MAXIMO_CAMPO_500))
        .ifPresent(country::setName);

    Optional.ofNullable(countryType.getIdentificationCode())
        .map(
            code -> ComunHelper.limitarRegistro(code.getValue(), Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(country::setIdentificationCode);

    return country;
  }
}
