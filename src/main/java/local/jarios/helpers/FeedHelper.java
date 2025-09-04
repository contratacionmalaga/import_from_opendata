package local.jarios.helpers;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiUnmarshallerException;
import local.jarios.interfaces.FeedSource;
import local.jarios.mappers.MapperFeed;
import local.jarios.parsers.LocalFeedSource;
import local.jarios.parsers.RemoteFeedSource;
import lombok.extern.slf4j.Slf4j;
import org.w3._2005.atom.FeedType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz para acciones sobre objetos Feed
 */
@Slf4j
public final class FeedHelper {

  /**
   * Constructor privado
   */
  private FeedHelper() {
    //
  }

  // ==========================
  // MÉTODOS PÚBLICOS
  // ==========================

  public static void parsearFeedsDesdeLocal() throws MiParseException {

    //
    parsearFeeds(new LocalFeedSource());
  }

  public static void parsearFeedsDesdeInternet()
      throws MiParseException {

    //
    parsearFeeds(new RemoteFeedSource());
  }

  /**
   * Obtiene la lista de feeds parseados desde local o remoto.
   */
  private static void parsearFeeds(FeedSource source)
      throws MiParseException {

    // Definición de variables locales
    boolean superadoNewestEntry = false;

    try {

      var unmarshaller = getValidUnmarshaller();
      log.debug("[parsearFeeds] - Obtención de objeto Unmarshaller correctamente.");
      String nextLink = source.getInitialLink();

      // Salimos si nextLink no es válido desde el inicio
      if (!source.isNextLinkValid(nextLink)) {
        log.warn("[parsearFeeds] - El NextLink inicial no es válido, se aborta el bucle: {}",
                 nextLink);
      }

      while (source.isNextLinkValid(nextLink) && (!superadoNewestEntry)) {

        try (var reader = source.openBufferedReader(nextLink)) {

          log.info("[parsearFeeds] - **** Parseando el feed '{}' ****", nextLink);

          // Obtengo el FeedType desde el fichero Atom
          var feedType = getFeedType(unmarshaller, reader);
          log.debug(
              "[parsearFeeds] - Obtenido el objeto FeedType desde el fichero Atom correctamente.");

          // Mapeo el fichero FeedType al objeto Feed
          var feed = MapperFeed.getFeed(feedType);
          log.debug("[parsearFeeds] - Procesado del feed correctamente.");

          List<Entry> listEntry = feed.getListEntry();
          log.info("[parsearFeeds] - Nº de objetos Entry en en Feed: {}.", listEntry.size());

          // Proceso los Entrys del objeto Feed devolviendo TRUE | FALSE según se haya superado el valor
          //      de updated asocaido al newestEntry
          superadoNewestEntry = EntryHelper.procesarListaEntry(listEntry);

          //
          if (!superadoNewestEntry) {
            nextLink = source.getNextLink(feed);
          }
        }
      }

    } catch (Exception e) {
      throw new MiParseException(e);
    }
  }

  // ==========================
  // MÉTODOS PRIVADOS
  // ==========================

  @SuppressWarnings("unchecked")
  private static FeedType getFeedType(Unmarshaller unmarshaller, BufferedReader reader) throws JAXBException {
    FeedType feedType = ((JAXBElement<FeedType>) unmarshaller.unmarshal(reader)).getValue();
    log.debug("[getFeedType] - Updated FeedType: {}",
              feedType.getUpdated().getValue().toGregorianCalendar().toString());
    return feedType;
  }

  private static Unmarshaller getValidUnmarshaller() throws MiUnmarshallerException {
    var unmarshaller = UnmarshallerHelper.getUnmarshaller();
    if (unmarshaller == null) {
      throw new MiUnmarshallerException("Unmarshaller nulo. No se puede continuar.");
    }
    return unmarshaller;
  }

  private static boolean isFechaValida(LocalDateTime updatedFeed, Feed newestFeed) {
    if (updatedFeed == null) return false;

    boolean despuesDeNewest = updatedFeed.isAfter(newestFeed.getUpdated());
    log.debug("[isFechaValida] - UpdatedFeed.isAfter(NewestFeed): {}", despuesDeNewest);
    boolean dentroDelRango = estaDentroDelRango(updatedFeed);
    log.debug("[isFechaValida] - estaDentroDelRango(updatedFeed): {}", dentroDelRango);
    return despuesDeNewest && dentroDelRango;
  }

  private static boolean estaDentroDelRango(LocalDateTime fecha) {
    log.debug("[estaDentroDelRango] - FechaInicial: {}", VariablesGlobales.getFiltroFechaInicial());
    log.debug("[estaDentroDelRango] - FechaInicial: {}", VariablesGlobales.getFiltroFechaInicial());
    log.debug("[estaDentroDelRango] - Fecha: {}", fecha);

    boolean enRango = fecha.isBefore(VariablesGlobales.getFiltroFechaInicial()) &&
        fecha.isAfter(VariablesGlobales.getFiltroFechaFinal());
    log.debug("[estaDentroDelRango] - estaDentroDelRango(Fecha): {}", enRango);
    return enRango;
  }
}
