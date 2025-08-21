package local.jarios.helpers;

import local.jarios.common.util.Constantes;
import local.jarios.exceptions.MiUnknownHostException;
import local.jarios.managers.ManagerJackson;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Interfaz para acciones comunes
 */
@Slf4j
public final class ComunHelper {

  /**
   * CONSTRUCTOR PRIVADO DE LA CLASE PUESTO QUE ESTA FINAL
   */
  private ComunHelper() {
  }

  /**
   * Devuelve el nombre del equipo que está ejecutando el código
   *
   * @return String con el nombre del Equipo
   * @throws MiUnknownHostException Excepción en caso de no poder acceder
   */
  public static String getHostName() throws MiUnknownHostException {

    //
    try {
      //
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException ex) {
      //
      log.info(ex.getMessage());
      //
      throw new MiUnknownHostException(ex);
    }
  }

  public static String limitarRegistro(String campo, int tamanoMaximo) {

    //
    if (campo == null || campo.isBlank()) {

      //
      return Constantes.CADENA_VACIA;

    } else {

      //
      if (campo.length() > tamanoMaximo) {

        //
        campo = campo.substring(0, tamanoMaximo);
      }

      //
      return campo;
    }
  }

  /**
   * Función encargada de imprimir un objeto
   *
   * @param object El objeto que voy a imprimir
   */
  public static void imprimir(Object object) {
    Arrays
        .stream(ManagerJackson.objectToJsonPretty(object).split(Constantes.CR))
        .forEach(log::info);
  }

  /**
   * Devuelve una cadena con la fecha y hora formateada en el patrón "yyyy-MM-dd HH:mm:ss".
   * Si el parámetro es null, usa la fecha y hora actual.
   *
   * @param localDateTime El objeto {@link LocalDateTime} a formatear, o null para usar la fecha/hora actual.
   * @return Fecha y hora formateada como cadena.
   */
  public static String getFechaHoraFormateada(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return Constantes.NULL;
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constantes.DATE_TIME_PATTERN);
    return localDateTime.format(formatter);
  }
}
