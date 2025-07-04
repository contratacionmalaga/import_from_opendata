package local.jarios.helpers;

import local.jarios.common.util.*;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.*;
import local.jarios.interfaces.FeedSource;
import local.jarios.mappers.MapperFeed;
import local.jarios.parsers.LocalFeedSource;
import local.jarios.parsers.RemoteFeedSource;
import local.jarios.services.ServicePrincipal;
import local.jarios.services.ServicePrincipalImpl;
import lombok.extern.slf4j.Slf4j;
import org.w3._2005.atom.FeedType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public final class FeedHelper {

    private FeedHelper() { }

    // ==========================
    // MÉTODOS PÚBLICOS
    // ==========================

    public static void parsearFeedsDesdeLocal(Log miLog, Estadistica estadistica)
            throws MiParseException {

        //
        parsearFeeds(miLog, estadistica, new LocalFeedSource(), null);
    }

    public static void parsearFeedsDesdeInternet(Log miLog, Feed newestFeed, Estadistica estadistica)
            throws MiParseException {

        //
        parsearFeeds(miLog, estadistica, new RemoteFeedSource(), newestFeed);
    }

    /**
     * Obtiene la lista de feeds parseados desde local o remoto.
     */
    private static void parsearFeeds(Log miLog, Estadistica estadistica, FeedSource source, Feed newestFeed)
            throws MiParseException {

        log.trace("[parsearFeeds] - Inicio el parseo de los Feeds.");
        int totalEntries = 0, totalFeeds = 0;

        try {
            var unmarshaller = getValidUnmarshaller();
            String nextLink = source.getInitialLink();
            log.info("[parsearFeeds] - NextLink: {}", nextLink);

            while (source.isNextLinkValid(nextLink)) {
                try (var reader = source.openBufferedReader(nextLink)) {
                    var feedType = getFeedType(unmarshaller, reader);
                    var feed = MapperFeed.getFeed(miLog, feedType);

                    log.info("[parsearFeeds] - Feed parseado con LinkSelf: {}", feed.getLinkSelf());
                    estadistica.aumentarNFicheros();
                    totalFeeds++;

                    boolean fechaValida = newestFeed == null || isFechaValida(feed.getUpdated(), newestFeed);
                    if (fechaValida) {
                        procesarFeed(feed, estadistica);
                        totalEntries += feed.getListEntry().size();
                    } else {
                        log.info("[parsearFeeds] - Feed con fecha inválida, deteniendo.");
                        break;
                    }

                    nextLink = source.getNextLink(feed);
                    log.info("[parsearFeeds] - NextLink: {}", nextLink);
                }
            }

            log.info("[parsearFeeds] - Feeds procesados: {}, Entradas: {}", totalFeeds, totalEntries);

        } catch (Exception e) {
            throw new MiParseException(e);
        }
    }


    /**
     * Devuelve el feed más reciente en base al tipo de sindicación.
     */
    public static Feed getNewestFeed(TipoSindicacion tipoSindicacion) throws MiServiceException {
        ServicePrincipal servicePrincipal = new ServicePrincipalImpl();
        Feed feed = servicePrincipal.getNewestFeed(tipoSindicacion);
        log.debug("[getNewestFeed] - TipoSindicacion: {}. NewestFeed: {}", tipoSindicacion, feed);
        return feed;
    }

    // ==========================
    // MÉTODOS PRIVADOS
    // ==========================

    private static void procesarFeed(Feed feed, Estadistica estadistica) throws MiInvalidDateFormatException {
        List<Entry> entries = feed.getListEntry();
        estadistica.setNEntryLeidos(estadistica.getNEntryLeidos() + entries.size());
        log.debug("[procesarFeed] - estadistica.setNEntryLeidos({})", estadistica.getNEntryLeidos() + entries.size());

        for (Entry entry : entries) {
            EntryHelper.procesarEntry(entry, estadistica);
        }
    }

    @SuppressWarnings("unchecked")
    private static FeedType getFeedType(Unmarshaller unmarshaller, BufferedReader reader) throws JAXBException {
        FeedType feedType = ((JAXBElement<FeedType>) unmarshaller.unmarshal(reader)).getValue();
        log.debug("[getFeedType] - Updated FeedType: {}", feedType.getUpdated().getValue().toGregorianCalendar().toString());
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

        boolean despuesDeNewest = newestFeed == null || updatedFeed.isAfter(newestFeed.getUpdated());
        boolean dentroDelRango = estaDentroDelRango(updatedFeed);
        return despuesDeNewest && dentroDelRango;
    }

    private static boolean estaDentroDelRango(LocalDateTime fecha) {
        return (VariablesGlobales.getFiltroFechaInicial() == null || !fecha.isBefore(VariablesGlobales.getFiltroFechaInicial())) &&
                (VariablesGlobales.getFiltroFechaFinal() == null || !fecha.isAfter(VariablesGlobales.getFiltroFechaFinal()));
    }
}
