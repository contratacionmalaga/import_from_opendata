package local.jarios.helpers;

import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiUnmarshallerException;
import local.jarios.filtro.FiltroManager;
import local.jarios.interfaces.FeedSource;
import local.jarios.mappers.atom.MapperFeed;
import local.jarios.parsers.LocalFeedSource;
import local.jarios.parsers.RemoteFeedSource;
import lombok.extern.slf4j.Slf4j;
import org.w3._2005.atom.FeedType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
      log.debug("Obtención de objeto Unmarshaller correctamente.");
      String nextLink = source.getInitialLink();

      // Salimos si nextLink no es válido desde el inicio
      if (!source.isNextLinkValid(nextLink)) {
        log.error("El NextLink inicial no es válido: {}.", nextLink);
      }

      while (source.isNextLinkValid(nextLink) && (!superadoNewestEntry)) {

        try (BufferedReader reader = source.openBufferedReader(nextLink)) {

          log.info("**** Parseando el feed '{}' ****", nextLink);

          FeedType feedType = getFeedType(unmarshaller, reader);
          log.info("  Obtenido el objeto FeedType desde el fichero Atom correctamente.");

          Feed feed = MapperFeed.getFeed(feedType);
          log.info("  Obtenido el objeto Feed desde el objeto FeedType.");

          List<Entry> listEntry = feed.getEntryList();
          log.info(
              "  Obtengo la lista de Entry asociada al Feed: {} elementos.",
              listEntry.size()
          );

          List<DeletedEntry> listDeletedEntry = feed.getDeletedEntryList();
          log.info(
              "  Obtengo la lista de DeletedEntry asociada al Feed: {} elementos.",
              listDeletedEntry.size()
          );

          feed.setEntryList(new ArrayList<>());
          log.info("  Borro la lista de Entry asociado al Feed antes de añadirlo al conjunto.");

          VariablesGlobales.getSetFeedsFromAtoms().add(feed);
          log.info("  Añadido el Feed al conjunto de Feeds correctamente.");

          // Proceso los Entrys del objeto Feed devolviendo TRUE | FALSE según se haya superado el valor
          //      de updated asocaido al newestEntry
          var processor = new EntryProcessor(
              new EntryProcessor.FiltroManagerEntryFilter(new FiltroManager()),
              new VariablesGlobalesEntryState()
          );
          superadoNewestEntry = processor.processEntries(listEntry);

          //
          if (!superadoNewestEntry) {
            nextLink = source.getNextLink(feed);
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
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
