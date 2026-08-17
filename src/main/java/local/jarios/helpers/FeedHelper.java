package local.jarios.helpers;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiUnmarshallerException;
import local.jarios.filtro.FiltroManager;
import local.jarios.interfaces.FeedSource;
import local.jarios.mappers.atom.MapperFeed;
import local.jarios.parsers.InternetFeedSource;
import local.jarios.parsers.LocalFeedSource;
import lombok.extern.slf4j.Slf4j;
import org.w3._2005.atom.FeedType;

@Slf4j
public final class FeedHelper {

  private FeedHelper() {}

  public static void parsearFeedsDesdeLocal(OpenDataExecutionContext context)
      throws MiParseException {
    parsearFeeds(new LocalFeedSource(context), context);
  }

  public static void parsearFeedsDesdeInternet(OpenDataExecutionContext context)
      throws MiParseException {
    parsearFeeds(new InternetFeedSource(context), context);
  }

  private static void parsearFeeds(FeedSource source, OpenDataExecutionContext context)
      throws MiParseException {

    boolean superadoNewestEntry = false;
    int feedsParseados = 0;
    long totalEntries = 0;
    long totalDeletedEntries = 0;

    try {
      var unmarshaller = getValidUnmarshaller();
      log.info("Obtención de objeto Unmarshaller correctamente.");

      String nextLink = source.getInitialLink();
      if (!source.isNextLinkValid(nextLink)) {
        log.info(
            "No se puede iniciar el parseo. La URL/NextLink inicial no es válida: {}", nextLink);
        return;
      }

      FiltroManager filtroManager = new FiltroManager();

      while (source.isNextLinkValid(nextLink) && !superadoNewestEntry) {
        try (BufferedReader reader = source.openBufferedReader(nextLink)) {
          log.debug("Parseando el feed '{}'", nextLink);

          FeedType feedType = getFeedType(unmarshaller, reader);
          Feed feed = MapperFeed.getFeed(feedType);
          feedsParseados++;

          List<Entry> listEntry = feed.getEntryList();
          totalEntries += listEntry.size();
          log.debug("  * Lista de Entry: {} elementos.", listEntry.size());

          List<DeletedEntry> listDeletedEntry = feed.getDeletedEntryList();
          totalDeletedEntries += listDeletedEntry.size();
          log.debug("  * Lista de DeletedEntry: {} elementos.", listDeletedEntry.size());
          log.info(
              "Feed parseado {}: entries={}, deletedEntries={}",
              feedsParseados,
              listEntry.size(),
              listDeletedEntry.size());

          feed.setEntryList(new ArrayList<>());
          context.getConjuntoFeedsFromAtoms().add(feed);

          if (!listDeletedEntry.isEmpty()) {
            for (DeletedEntry deletedEntry : listDeletedEntry) {
              if (deletedEntry != null && deletedEntry.getRef() != null) {
                context.getMapDeletedEntriesFromAtoms().put(deletedEntry.getRef(), deletedEntry);
              }
            }
          }

          var processor =
              new EntryProcessor(
                  context,
                  new EntryProcessor.FiltroManagerEntryFilter(context, filtroManager),
                  new ContextEntryState(context));

          superadoNewestEntry = processor.processEntries(listEntry);

          if (!superadoNewestEntry) {
            try {
              nextLink = source.getNextLink(feed);

              if (!source.isNextLinkValid(nextLink)) {
                log.info("Fin del parseo. La URL/NextLink no es válida: {}", nextLink);
                break;
              }

              log.debug("  Próximo feed a parsear: {}", nextLink);
            } catch (Exception ex) {
              log.info("Fin del parseo. La URL/NextLink no es válida: {}", ex.getMessage());
              break;
            }
          }
        }
      }

      log.info(
          "Parseo de feeds finalizado: feeds={}, entries={}, deletedEntries={}",
          feedsParseados,
          totalEntries,
          totalDeletedEntries);
    } catch (Exception ex) {
      log.error("Error durante el parseo de los ficheros feed. {}", ex.getMessage(), ex);
      throw new MiParseException(ex);
    }
  }

  @SuppressWarnings("unchecked")
  private static FeedType getFeedType(Unmarshaller unmarshaller, BufferedReader reader)
      throws JAXBException {
    FeedType feedType = ((JAXBElement<FeedType>) unmarshaller.unmarshal(reader)).getValue();
    log.debug(
        "[getFeedType] - Updated FeedType: {}",
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
}
