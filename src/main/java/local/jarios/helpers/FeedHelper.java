package local.jarios.helpers;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
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

  static void parsearFeeds(FeedSource source, OpenDataExecutionContext context)
      throws MiParseException {

    boolean alcanzadoCorteIncremental = false;
    int feedsParseados = 0;
    long totalEntries = 0;
    long totalDeletedEntries = 0;
    long aperturaNanos = 0;
    long deserializacionYMapeoNanos = 0;
    long filtradoNanos = 0;
    Set<String> enlacesVisitados = new HashSet<>();

    try {
      var unmarshaller = getValidUnmarshaller();
      log.info("Obtención de objeto Unmarshaller correctamente.");

      String nextLink = source.getInitialLink();
      if (!source.isNextLinkValid(nextLink)) {
        throw new IllegalStateException(
            "No se puede iniciar el parseo. La URL/NextLink inicial no es válida: " + nextLink);
      }

      FiltroManager filtroManager = new FiltroManager();

      while (source.isNextLinkValid(nextLink)) {
        if (!enlacesVisitados.add(nextLink)) {
          throw new IllegalStateException("Ciclo detectado en la cadena de feeds: " + nextLink);
        }
        long inicioApertura = System.nanoTime();
        try (BufferedReader reader = source.openBufferedReader(nextLink)) {
          aperturaNanos += System.nanoTime() - inicioApertura;
          log.debug("Parseando el feed '{}'", nextLink);

          long inicioDeserializacionYMapeo = System.nanoTime();
          FeedType feedType = getFeedType(unmarshaller, reader);
          Feed feed = MapperFeed.getFeed(feedType);
          deserializacionYMapeoNanos += System.nanoTime() - inicioDeserializacionYMapeo;
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

          long inicioFiltrado = System.nanoTime();
          boolean corteAlcanzadoEnEsteFeed = processor.processEntries(listEntry);
          alcanzadoCorteIncremental |= corteAlcanzadoEnEsteFeed;
          filtradoNanos += System.nanoTime() - inicioFiltrado;

          if (corteAlcanzadoEnEsteFeed) {
            log.debug(
                "Corte incremental alcanzado en el feed {}; se sigue navegando para recoger bajas posteriores.",
                feedsParseados);
          }

          if (feed.getLinkNext() == null || feed.getLinkNext().isBlank()) {
            log.info("Fin normal del parseo: el feed no informa enlace next.");
            break;
          }

          nextLink = source.getNextLink(feed);
          if (!source.isNextLinkValid(nextLink)) {
            throw new IllegalStateException("Enlace next informado pero no válido: " + nextLink);
          }

          log.debug("  Próximo feed a parsear: {}", nextLink);
        }
      }

      log.info(
          "Parseo de feeds finalizado: feeds={}, entries={}, deletedEntries={}, corteIncremental={}, apertura={}, JAXB+mapeo={}, filtrado={}",
          StringHelper.getNumeroConFormato(feedsParseados),
          StringHelper.getNumeroConFormato(totalEntries),
          StringHelper.getNumeroConFormato(totalDeletedEntries),
          alcanzadoCorteIncremental,
          LocalDateTimeHelper.getDiferenciaNanos(0, aperturaNanos),
          LocalDateTimeHelper.getDiferenciaNanos(0, deserializacionYMapeoNanos),
          LocalDateTimeHelper.getDiferenciaNanos(0, filtradoNanos));
    } catch (Exception ex) {
      log.error("Error durante el parseo de los ficheros feed. {}", ex.getMessage(), ex);
      throw new MiParseException(ex);
    }
  }

  @SuppressWarnings("unchecked")
  private static FeedType getFeedType(Unmarshaller unmarshaller, BufferedReader reader)
      throws JAXBException {
    XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
    xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

    try {
      XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(reader);
      FeedType feedType = ((JAXBElement<FeedType>) unmarshaller.unmarshal(xmlReader)).getValue();
      log.debug(
          "[getFeedType] - Updated FeedType: {}",
          feedType.getUpdated().getValue().toGregorianCalendar().toString());
      return feedType;
    } catch (XMLStreamException ex) {
      throw new JAXBException("No se ha podido leer el XML ATOM de forma segura", ex);
    }
  }

  private static Unmarshaller getValidUnmarshaller() throws MiUnmarshallerException {
    var unmarshaller = UnmarshallerHelper.getUnmarshaller();
    if (unmarshaller == null) {
      throw new MiUnmarshallerException("Unmarshaller nulo. No se puede continuar.");
    }
    return unmarshaller;
  }
}
