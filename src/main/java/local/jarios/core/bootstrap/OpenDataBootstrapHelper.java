package local.jarios.core.bootstrap;

import java.util.Arrays;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OpenDataBootstrapHelper {

  private OpenDataBootstrapHelper() {}

  public static String resolveConfigDir(String[] args) {
    return Arrays.stream(args)
        .filter(arg -> arg.startsWith("--configDir="))
        .map(arg -> arg.substring("--configDir=".length()))
        .findFirst()
        .orElseGet(
            () -> {
              log.info(
                  "No se especificó el parámetro '--configDir='. Usando el valor por defecto: {}",
                  Constantes.PROPERTIES_DIR);
              return Constantes.PROPERTIES_DIR;
            });
  }
}
