package local.jarios.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Feed;
import local.jarios.exceptions.MiUrlException;
import local.jarios.helpers.FeedLocationHelper;
import local.jarios.interfaces.FeedSource;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de {@link FeedSource} para obtener feeds remotos a través de URLs.
 *
 * <p>Esta clase ofrece:
 *
 * <ul>
 *   <li>Validación centralizada y robusta de URLs conforme a la especificación RFC.
 *   <li>Evitación del uso de constructores URL(String) deprecated.
 *   <li>Logging detallado y manejo exhaustivo de excepciones propias.
 *   <li>Validación segura en {@link #isNextLinkValid(String)} para evitar fallos ante URLs
 *       inválidas.
 * </ul>
 *
 * <p>La configuración de la URL inicial se obtiene de un archivo de propiedades gestionado mediante
 * {@link PropertiesManagerService}. Autor: juan Fecha: 04/07/2025 (mejorado 05/07/2025)
 */
@Slf4j
public class InternetFeedSource implements FeedSource {

  private static final int DEFAULT_MAX_RETRIES = 3;
  private static final long DEFAULT_RETRY_DELAY_MS = 60_000L;
  private static final long DEFAULT_REQUEST_DELAY_MS = 0L;

  private final OpenDataExecutionContext context;
  private final PropertiesManagerService propertiesManager;
  private final HttpClient client;

  public InternetFeedSource(OpenDataExecutionContext context) {
    this.context = Objects.requireNonNull(context, "context");
    this.propertiesManager = PropertiesManagerServiceImpl.getInstance();
    this.client =
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
  }

  /**
   * Valida una URL.
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
      validateHttpUri(uri);
      URL validatedUrl = uri.toURL();
      log.debug("[validateUrl] URL válida (convertida a URL): {}", validatedUrl);
    } catch (Exception e) {
      String msg = "[validateUrl] URL inválida: " + url;
      log.error(msg, e);
      throw new MiUrlException(msg, e);
    }
  }

  private static void validateHttpUri(URI uri) throws MiUrlException {
    String scheme = uri.getScheme();
    if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
      throw new MiUrlException("[validateUrl] Solo se permiten URLs http/https: " + uri);
    }

    if (uri.getHost() == null || uri.getHost().isBlank()) {
      throw new MiUrlException("[validateUrl] URL sin host valido: " + uri);
    }

    if (uri.getUserInfo() != null) {
      throw new MiUrlException("[validateUrl] URL con user-info no permitida: " + uri);
    }
  }

  /**
   * Obtiene la URL inicial para la carga del feed desde el archivo de propiedades.
   *
   * @return La URL inicial como {@link String}.
   * @throws PropertiesManagerException si hay error al acceder a la propiedad.
   * @throws MiUrlException si la URL obtenida es nula, vacía o inválida.
   */
  @Override
  public String getInitialLink() throws PropertiesManagerException, MiUrlException {
    try {
      return FeedLocationHelper.getInitialLink(
          context.getLugarImportacion(), context.getTipoSindicacion());
    } catch (PropertiesManagerException ex) {
      throw new IllegalStateException("No se pudo resolver la URL remota inicial", ex);
    }
  }

  /**
   * Valida si la URL para el siguiente enlace es válida.
   *
   * <p>Esta implementación captura errores de validación y no lanza excepciones, devolviendo {@code
   * false} si la URL no es válida.
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
    log.debug("[getNextLink] nextLink del Feed '{}': {}", feed, url);
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
      validateHttpUri(uri);

      int maxRetries = readIntProperty(PropertiesKeys.APP_HTTP_MAX_RETRIES, DEFAULT_MAX_RETRIES);
      long retryDelayMs =
          readLongProperty(PropertiesKeys.APP_HTTP_RETRY_DELAY_MS, DEFAULT_RETRY_DELAY_MS);
      long requestDelayMs =
          readLongProperty(PropertiesKeys.APP_HTTP_REQUEST_DELAY_MS, DEFAULT_REQUEST_DELAY_MS);

      IOException lastError = null;
      for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
        sleepBeforeRequest(requestDelayMs);
        HttpResponse<String> response = sendRequest(uri);
        String body = response.body();

        log.info(
            "Feed remoto descargado: status={}, bytes={}", response.statusCode(), body.length());

        if (isAtomResponse(response, body)) {
          return new BufferedReader(new StringReader(body));
        }

        lastError =
            new IOException(
                "Respuesta remota no ATOM. status="
                    + response.statusCode()
                    + ", contentType="
                    + response.headers().firstValue("Content-Type").orElse("")
                    + ", snippet="
                    + bodySnippet(body));

        if (attempt > maxRetries) {
          break;
        }

        long delay = retryDelayMs * attempt;
        log.warn(
            "Respuesta remota no válida para {}. Reintento {}/{} en {} ms. {}",
            uri,
            attempt,
            maxRetries,
            delay,
            lastError.getMessage());
        sleep(delay);
      }

      throw lastError;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      String msg = "[openBufferedReader] Descarga interrumpida para URL: " + path;
      log.error(msg, e);
      throw new MiUrlException(msg, e);
    } catch (Exception e) {
      String msg = "[openBufferedReader] Error al abrir BufferedReader para URL: " + path;
      log.error(msg, e);
      throw new MiUrlException(msg, e);
    }
  }

  private HttpResponse<String> sendRequest(URI uri) throws IOException, InterruptedException {
    log.info("Descargando feed remoto: {}", uri);
    HttpRequest request =
        HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(60))
            .header("Accept", "application/atom+xml, application/xml, text/xml;q=0.9, */*;q=0.1")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .GET()
            .build();
    return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private boolean isAtomResponse(HttpResponse<String> response, String body) {
    if (response.statusCode() != 200 || body == null || body.isBlank()) {
      return false;
    }

    String contentType =
        response.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
    if (contentType.contains("html")) {
      return false;
    }

    String trimmed = body.stripLeading().toLowerCase(Locale.ROOT);
    return trimmed.startsWith("<?xml") || trimmed.startsWith("<feed");
  }

  private void sleepBeforeRequest(long requestDelayMs) throws InterruptedException {
    if (requestDelayMs > 0) {
      sleep(requestDelayMs);
    }
  }

  private void sleep(long millis) throws InterruptedException {
    if (millis > 0) {
      Thread.sleep(millis);
    }
  }

  private int readIntProperty(String key, int defaultValue) {
    return (int) readLongProperty(key, defaultValue);
  }

  private long readLongProperty(String key, long defaultValue) {
    try {
      String value = propertiesManager.getProperty(PropertiesFiles.APP, key);
      if (value == null || value.isBlank()) {
        return defaultValue;
      }
      return Long.parseLong(value.trim());
    } catch (Exception ex) {
      log.debug("Usando valor por defecto para {}: {}", key, defaultValue);
      return defaultValue;
    }
  }

  private String bodySnippet(String body) {
    if (body == null) {
      return "";
    }
    String normalized = body.replaceAll("\\s+", " ").trim();
    int maxLength = Math.min(180, normalized.length());
    return normalized.substring(0, maxLength);
  }
}
