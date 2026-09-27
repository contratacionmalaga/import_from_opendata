package local.jarios.interfaces;

import java.io.BufferedReader;
import local.jarios.entity.atom.Feed;

/**
 * Interfaz genérica para definir la estructura común que deben implementar las clases que
 * proporcionan fuentes de datos tipo Feed. Proporciona métodos para manejar enlaces iniciales,
 * enlaces siguientes y la apertura de streams para lectura.
 *
 * @author Juan
 * @since 2024-06-04
 */
public interface FeedSource {

  /**
   * Obtiene el enlace inicial único que identifica la fuente de datos.
   *
   * @return enlace inicial como {@code String}
   * @throws Exception si ocurre algún error al obtener el enlace
   */
  String getInitialLink() throws Exception;

  /**
   * Valida si un enlace siguiente es válido para procesar.
   *
   * @param link enlace siguiente a validar
   * @return {@code true} si el enlace es válido; {@code false} en caso contrario
   * @throws Exception si ocurre algún error durante la validación
   */
  boolean isNextLinkValid(String link) throws Exception;

  /**
   * Indica si un enlace siguiente no disponible constituye el final normal de una fuente.
   *
   * <p>El valor predeterminado es {@code false}: una fuente remota o una ruta inválida debe
   * seguir tratándose como error. La fuente local puede aceptar un fichero siguiente ausente como
   * límite del corpus disponible.
   *
   * @param link enlace siguiente ya resuelto
   * @return {@code true} cuando debe finalizarse el parseo correctamente
   */
  default boolean isMissingNextLinkTerminal(String link) {
    return false;
  }

  /**
   * Obtiene el enlace siguiente a partir del {@link Feed} actual para continuar la lectura o
   * procesamiento.
   *
   * @param feed objeto Feed con la información actual
   * @return enlace siguiente como {@code String}
   * @throws Exception si ocurre algún error al obtener el enlace siguiente
   */
  String getNextLink(Feed feed) throws Exception;

  /**
   * Abre un {@link BufferedReader} para leer el contenido ubicado en la ruta dada.
   *
   * @param path ruta o URL del recurso a leer
   * @return {@link BufferedReader} para lectura
   * @throws Exception si ocurre algún error al abrir el lector
   */
  BufferedReader openBufferedReader(String path) throws Exception;
}
