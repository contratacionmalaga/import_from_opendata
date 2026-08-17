package local.jarios.helpers;

import java.util.Objects;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Ayuda en la obtención del tipo de sindicación (local o remota) a partir de propiedades. Clase
 * utilitaria de solo métodos estáticos y constructor privado.
 *
 * @author Juan
 */
@Slf4j
public final class TipoSindicacionHelper {

  /** Variable asociada al servicio de consulta de los ficheros properties */
  private final PropertiesManagerService propertyManager;

  public TipoSindicacionHelper() {
    this.propertyManager = PropertiesManagerServiceImpl.getInstance();
  }

  /**
   * Mapea un nombre de fichero a un tipo de sindicación.
   *
   * @param value nombre
   * @return TipoSindicacion correspondiente, o ERROR si no coincide
   */
  private static TipoSindicacion mapToTipo(String value) {
    if (value == null || value.isBlank()) {
      return TipoSindicacion.ERROR;
    }

    try {
      return TipoSindicacion.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return TipoSindicacion.ERROR;
    }
  }

  /**
   * Determina el tipo de sindicación local leyendo la propiedad APP_FILENAME.
   *
   * @return Tipo de sindicación según el nombre de fichero en propiedades
   * @throws PropertiesManagerException si la propiedad es inválida o no existe
   */
  public TipoSindicacion getTipoSindicacionDesdeProperties() throws PropertiesManagerException {

    String tipoSindicacion =
        propertyManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_TIPO_SINDICACION);
    log.debug(
        "Variables leídas: Fichero: {}, Propiedad: {}, Valor: {}",
        PropertiesFiles.APP,
        PropertiesKeys.APP_TIPO_SINDICACION,
        tipoSindicacion);
    Objects.requireNonNull(
        tipoSindicacion,
        String.format("La propiedad %s no puede estar vacía", PropertiesKeys.APP_TIPO_SINDICACION));
    TipoSindicacion tipo = mapToTipo(tipoSindicacion);
    log.debug("Tipo de sindicación -> {}", tipo);
    return tipo;
  }
}
