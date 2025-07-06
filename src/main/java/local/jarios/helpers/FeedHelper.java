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
import java.util.Comparator;
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

    public static void parsearFeedsDesdeInternet(Log miLog, Entry newestEntry, Estadistica estadistica)
            throws MiParseException {

        //
        parsearFeeds(miLog, estadistica, new RemoteFeedSource(), newestEntry);
    }

    /**
     * Obtiene la lista de feeds parseados desde local o remoto.
     */
    private static void parsearFeeds(Log miLog, Estadistica estadistica, FeedSource source, Entry newestEntry)
            throws MiParseException {

        // Definición de variables locales
        boolean superadoNewestEntry = false;

        try {
            var unmarshaller = getValidUnmarshaller();
            log.info("[parsearFeeds] - Obtención de objeto Unmarshaller correctamente.");
            String nextLink = source.getInitialLink();

            // Salimos si nextLink no es válido desde el inicio
            if (!source.isNextLinkValid(nextLink)) {
                log.warn("[parsearFeeds] - El NextLink inicial no es válido, se aborta el bucle: {}", nextLink);
            }

            while (source.isNextLinkValid(nextLink) && (!superadoNewestEntry)) {

                try (var reader = source.openBufferedReader(nextLink)) {
                    // Obtengo el FeedType desde el fichero Atom
                    var feedType = getFeedType(unmarshaller, reader);
                    log.info("[parsearFeeds] - Obtenido el objeto FeedType desde el fichero Atom correctamente.");

                    // Mapeo el fichero FeedType al objeto Feed
                    var feed = MapperFeed.getFeed(miLog, feedType);
                    log.info("[parsearFeeds] - Parseo correcto del FeedType. '{}' objetos Entry.",
                            StringHelper.getNumeroConFormato(feed.getListEntry().size()));

                    // Aumento el NFicheros procesados
                    estadistica.aumentarNFicheros();

                    // Proceso los Entrys del objeto Feed
                    superadoNewestEntry = procesarFeed(feed, estadistica, newestEntry);
                    if (!superadoNewestEntry) {
                        nextLink = source.getNextLink(feed);
                        log.info(
                                "[parsearFeeds] - Loop - nextLink = {}. ¿Es válido? {}",
                                nextLink, source.isNextLinkValid(nextLink));
                    }
                }
            }

            log.info("[parsearFeeds] - {}", estadistica.toStringReducido());

        } catch (Exception e) {
            throw new MiParseException(e);
        }
    }

    // ==========================
    // MÉTODOS PRIVADOS
    // ==========================

    private static boolean procesarFeed(Feed feed, Estadistica estadistica, Entry newestEntry)
            throws MiInvalidDateFormatException {

        // Obtengo la lista
        List<Entry> entries = feed.getListEntry();

        // Ordeno la lista de mayor a menor según Updated
        entries.sort(Comparator.comparing(Entry::getUpdated).reversed());
        log.info("[procesarFeed] - Ordeno la lista de Entrys mediante el campo Updated (DESC).");

        estadistica.setNEntryLeidos(estadistica.getNEntryLeidos() + entries.size());

        boolean superadoNewestEntry = false;
        int nEntryProcesadosAxu = estadistica.getNEntryProcesados();

        for (Entry entry : entries) {
            if (newestEntry == null || entry.getUpdated().isAfter(newestEntry.getUpdated())) {
                EntryHelper.procesarEntry(entry, estadistica);
                estadistica.aumentarNEntryProcesados();
            } else {
                superadoNewestEntry = true;
                break;
            }
        }

        //
        log.info(
                "[procesarFeed] - Entrys procesados: {}",
                StringHelper.getNumeroConFormato(estadistica.getNEntryProcesados() - nEntryProcesadosAxu));

        //
        log.info("[procesarFeed] - ¿Superado NewestEntry?: {}", superadoNewestEntry);
        return superadoNewestEntry;
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
