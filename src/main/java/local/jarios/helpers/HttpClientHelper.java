package local.jarios.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import local.jarios.exceptions.MiHttpException;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilidad para realizar operaciones HTTP relacionadas con la descarga de archivos.
 *
 * <p>Proporciona métodos estáticos para descargar archivos Excel desde URLs externas, manejando
 * cookies, redirecciones y tiempos de espera.
 *
 * <p>Esta clase es final y no puede ser instanciada.
 *
 * @author Juan
 * @since 2025-06-22
 */
@Slf4j
public final class HttpClientHelper {

  /** Constructor privado para evitar la creación de instancias. */
  private HttpClientHelper() {}

  /**
   * Descarga un archivo Excel desde la URL proporcionada.
   *
   * <p>Este método realiza una petición HTTP GET con manejo de cookies y redirecciones,
   * estableciendo un User-Agent estándar para evitar bloqueos por parte del servidor.
   *
   * <p>Se espera que el contenido descargado sea un archivo Excel con formato ZIP (.xlsx), aunque
   * esta validación no se realiza explícitamente.
   *
   * @param url URL completa del archivo Excel a descargar.
   * @return {@link InputStream} con el contenido del archivo Excel descargado.
   */
  public static InputStream downloadExcel(String url) {

    try {
      CookieManager cookieManager = new CookieManager();
      cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

      HttpClient client =
          HttpClient.newBuilder()
              .cookieHandler(cookieManager)
              .followRedirects(HttpClient.Redirect.ALWAYS)
              .connectTimeout(Duration.ofSeconds(10))
              .build();

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(url))
              .timeout(Duration.ofSeconds(20))
              .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
              .GET()
              .build();

      HttpResponse<InputStream> response =
          client.send(request, HttpResponse.BodyHandlers.ofInputStream());

      int status = response.statusCode();

      if (status != 200) {
        throw new IOException("Error HTTP al descargar Excel: código " + status);
      }

      return response.body();

    } catch (InterruptedException ex) {
      String msg =
          String.format(
              "[downloadExcel] - Interrupción durante la descarga del Excel. Error: %s",
              ex.getMessage());
      log.debug(msg, ex);
      throw new MiHttpException(msg, ex);
    } catch (Exception ex) {
      String msg =
          String.format(
              "[downloadExcel] - Fallo al descargar el Excel desde %s. Error: %s",
              url, ex.getMessage());
      log.debug(msg, ex);
      throw new MiHttpException(msg, ex);
    }
  }
}
