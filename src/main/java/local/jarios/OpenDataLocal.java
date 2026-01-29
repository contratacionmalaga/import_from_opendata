package local.jarios;

import local.jarios.common.util.Constantes;
import local.jarios.common.util.Mensajes;
import local.jarios.enums.LugarImportacion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.MiParseException;
import local.jarios.helpers.FeedHelper;
import local.jarios.helpers.StringHelper;
import local.jarios.helpers.TipoSindicacionHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * Implementación concreta del proceso de importación de datos para fuentes locales.
 * <p>
 * Esta clase extiende {@link AbstractOpenData} y define el comportamiento específico para la
 * sindicación de datos desde fuentes locales. Sobrescribe los métodos necesarios para identificar
 * el tipo de sindicación, el lugar de importación, y cómo se parsean los feeds.
 * </p>
 *
 * <p>
 * El punto de entrada {@code main} permite ejecutar el proceso completo llamando a
 * {@link AbstractOpenData#procesar(String configDir)}.
 * </p>
 *
 * @author Juan Antonio
 * @see AbstractOpenData
 * @see FeedHelper
 * @see TipoSindicacionHelper
 */
@Slf4j
public class OpenDataLocal extends AbstractOpenData {

  /**
   * Punto de entrada para ejecutar el proceso completo de importación desde fuentes locales. Crea
   * una instancia de esta clase y llama al método {@code procesar()} heredado.
   *
   * @param args argumentos de la línea de comandos (no se utilizan).
   */
  public static void main(String[] args) {

    // Inicio del log
    StringHelper.generarTitulo(log, Mensajes.INICIO_LOCAL);

    String configDir = Arrays.stream(args)
        .filter(arg -> arg.startsWith("--configDir="))
        .map(arg -> arg.substring("--configDir=".length()))
        .findFirst()
        .orElseGet(() -> {
          log.info("No se especificó el parámetro '--configDir='. Usando el valor por defecto: {}",
                   Constantes.PROPERTIES_DIR);
          return Constantes.PROPERTIES_DIR;
        });

    new OpenDataLocal().procesar(configDir);
  }

  /**
   * Obtiene el tipo de sindicación correspondiente a fuentes locales.
   *
   * @return el tipo de sindicación local definido en {@link TipoSindicacionHelper}.
   */
  @Override
  protected TipoSindicacion getTipoSindicacion() {

    //
    TipoSindicacionHelper tipoSindicacionHelper = new TipoSindicacionHelper();

    //
    return tipoSindicacionHelper.getTipoSindicacionLocal();
  }

  /**
   * Indica que el lugar de importación de esta clase es {@link LugarImportacion#LOCAL}.
   *
   * @return el valor {@code LugarImportacion.LOCAL}.
   */
  @Override
  protected LugarImportacion getLugarImportacion() {

    //
    return LugarImportacion.LOCAL;
  }

  /**
   * Realiza el parsing de los feeds Atom obtenidos localmente y actualiza la estadística.
   *
   * @throws MiParseException si ocurre un error durante el parsing de los feeds.
   */
  @Override
  protected void parsearAtomsFeeds() throws MiParseException {

    FeedHelper.parsearFeedsDesdeLocal();
  }
}
