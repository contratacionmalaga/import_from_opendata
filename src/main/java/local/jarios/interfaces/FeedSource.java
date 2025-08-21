package local.jarios.interfaces;

import local.jarios.entity.atom.Feed;

import java.io.BufferedReader;

/**
 * Interfaz genérica para definir la estructura común que deben implementar
 * las clases que proporcionan fuentes de datos tipo Feed.
 * Proporciona métodos para manejar enlaces iniciales, enlaces siguientes y
 * la apertura de streams para lectura.
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
   * Obtiene el enlace siguiente a partir del {@link Feed} actual para continuar
   * la lectura o procesamiento.
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
