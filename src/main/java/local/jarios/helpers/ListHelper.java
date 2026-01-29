package local.jarios.helpers;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Interfaz para acciones sobre objetos tipo List
 */
@Slf4j
public final class ListHelper {

  private ListHelper() {
    // NO IMPLEMENTADO
  }

  /**
   * Imprme una lista de string.
   *
   * @param list La lista a imprimir
   */
  public static void imprimirLista(List<String> list) {
    list.forEach(log::info);
  }
}
