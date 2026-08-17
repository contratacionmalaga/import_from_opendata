package local.jarios.helpers;

import local.jarios.common.util.Constantes;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;

public final class FeedLocationHelper {

  private FeedLocationHelper() {}

  public static String getInitialLink(LugarImportacion lugar, TipoSindicacion tipo)
      throws PropertiesManagerException {

    PropertiesManagerService pm = PropertiesManagerServiceImpl.getInstance();

    if (lugar == null || tipo == null) {
      throw new PropertiesManagerException("LugarImportacion o TipoSindicacion no informados.");
    }

    return switch (lugar) {
      case LOCAL -> buildLocalPath(pm, tipo);
      case INTERNET -> buildInternetUrl(pm, tipo);
    };
  }

  private static String buildLocalPath(PropertiesManagerService pm, TipoSindicacion tipo) {
    String rutaBase = pm.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_LOCAL_PATH);

    return switch (tipo) {
      case MAYORES ->
          rutaBase
              + Constantes.DIRECTORIO_MAY
              + pm.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_LOCAL_MAYORES);
      case MENORES ->
          rutaBase
              + Constantes.DIRECTORIO_MEN
              + pm.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_LOCAL_MENORES);
      case ENCARGOS ->
          rutaBase
              + Constantes.DIRECTORIO_EMP
              + pm.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_LOCAL_ENCARGOS);
      case CONSULTAS ->
          rutaBase
              + Constantes.DIRECTORIO_CPM
              + pm.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_LOCAL_CONSULTAS);
      case AGREGRADAS ->
          rutaBase
              + Constantes.DIRECTORIO_AGR
              + pm.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_LOCAL_AGREGADAS);
      case ERROR, TipoSindicacion -> null;
    };
  }

  private static String buildInternetUrl(PropertiesManagerService pm, TipoSindicacion tipo) {
    return switch (tipo) {
      case MAYORES -> pm.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_INTERNET_MAYORES);
      case MENORES -> pm.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_INTERNET_MENORES);
      case ENCARGOS -> pm.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_INTERNET_ENCARGOS);
      case CONSULTAS -> pm.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_INTERNET_CONSULTAS);
      case AGREGRADAS -> pm.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_INTERNET_AGREGADAS);
      case ERROR, TipoSindicacion -> null;
    };
  }
}
