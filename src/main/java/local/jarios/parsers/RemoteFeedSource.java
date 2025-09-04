package local.jarios.parsers;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.entity.atom.Feed;
import local.jarios.exceptions.MiUrlException;
import local.jarios.interfaces.FeedSource;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Implementación de {@link FeedSource} para obtener feeds remotos a través de URLs.
 * <p>
 * Esta clase ofrece:
 * <ul>
 *   <li>Validación centralizada y robusta de URLs conforme a la especificación RFC.</li>
 *   <li>Evitación del uso de constructores URL(String) deprecated.</li>
 *   <li>Logging detallado y manejo exhaustivo de excepciones propias.</li>
 *   <li>Validación segura en {@link #isNextLinkValid(String)} para evitar fallos ante URLs inválidas.</li>
 * </ul>
 * <p>La configuración de la URL inicial se obtiene de un archivo de propiedades gestionado mediante
 * {@link PropertiesManagerService}.</p>
 *
 * <p><b>Autor:</b> juan</p>
 * <p><b>Fecha:</b> 04/07/2025 (mejorado 05/07/2025)</p>
 */
@Slf4j
public class RemoteFeedSource implements FeedSource {

  /**
   * Servicio centralizado para la gestión y lectura de propiedades de configuración.
   */
  private final PropertiesManagerService propertyManager;

  /**
   * Constructor por defecto que obtiene la instancia singleton del gestor de propiedades.
   */
  public RemoteFeedSource() {
    this.propertyManager = PropertiesManagerServiceImpl.getInstance();
  }

  /**
   * Valida una URL según criterios:
   * <ul>
   *   <li>No puede ser nula o vacía.</li>
   *   <li>Debe tener sintaxis válida según RFC.</li>
   *   <li>Debe contener autoridad válida (host, puerto, etc.).</li>
   * </ul>
   *
   * @param url URL a validar.
   * @throws MiUrlException si la URL no cumple con los criterios anteriores.
   */
  private static void validateUrl(String url) throws MiUrlException {
    if (url == null || url.isBlank()) {
      String msg = "[validateUrl] URL nula o en blanco";
      log.error(msg);
      throw new MiUrlException(msg);
    }

    try {
      URI uri = new URI(url).parseServerAuthority();
      URL validatedUrl = uri.toURL();
      log.debug("[validateUrl] URL válida (convertida a URL): {}", validatedUrl);
    } catch (Exception e) {
      String msg = "[validateUrl] URL inválida: " + url;
      log.error(msg, e);
      throw new MiUrlException(msg, e);
    }
  }

  /**
   * Obtiene la URL inicial para la carga del feed desde el archivo de propiedades.
   *
   * @return La URL inicial como {@link String}.
   * @throws PropertiesManagerException si hay error al acceder a la propiedad.
   * @throws MiUrlException             si la URL obtenida es nula, vacía o inválida.
   */
  @Override
  public String getInitialLink() throws PropertiesManagerException, MiUrlException {
    String url = propertyManager.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_URL);
    log.debug("[getInitialLink] URL desde propiedades → {}", url);
    validateUrl(url);
    return url;
  }

  /**
   * Valida si la URL para el siguiente enlace es válida.
   * <p>
   * Esta implementación captura errores de validación y no lanza excepciones, devolviendo
   * {@code false} si la URL no es válida.
   * </p>
   *
   * @param link URL a validar.
   * @return {@code true} si la URL es válida; {@code false} en caso contrario.
   */
  @Override
  public boolean isNextLinkValid(String link) {
    log.debug("[isNextLinkValid] Validando nextLink → {}", link);
    try {
      validateUrl(link);
      return true;
    } catch (MiUrlException e) {
      log.warn("[isNextLinkValid] NextLink inválido: {}", link);
      return false;
    }
  }

  /**
   * Obtiene la URL del siguiente feed a partir del objeto {@link Feed}.
   *
   * @param feed Objeto {@link Feed} que contiene la URL siguiente.
   * @return La URL siguiente como {@link String}.
   * @throws MiUrlException si la URL obtenida es inválida.
   */
  @Override
  public String getNextLink(Feed feed) throws MiUrlException {
    String url = feed.getLinkNext();
    log.debug("[getNextLink] nextLink del Feed '{}': {}", feed.toStringResumido(), url);
    validateUrl(url);
    return url;
  }

  /**
   * Abre un {@link BufferedReader} para leer desde la URL proporcionada usando UTF-8.
   *
   * @param path URL del recurso a abrir.
   * @return {@link BufferedReader} para lectura del recurso.
   * @throws MiUrlException si la URL es inválida o no se puede abrir la conexión.
   */
  @Override
  public BufferedReader openBufferedReader(String path) throws MiUrlException {
    validateUrl(path);
    try {
      URI raw = new URI(path);
      URI uri = raw.parseServerAuthority();
      URL url = uri.toURL();
      log.debug("[openBufferedReader] Abriendo BufferedReader para URL → {}", uri);
      return new BufferedReader(
          new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
    } catch (Exception e) {
      String msg = "[openBufferedReader] Error al abrir BufferedReader para URL: " + path;
      log.error(msg, e);
      throw new MiUrlException(msg, e);
    }
  }
}
