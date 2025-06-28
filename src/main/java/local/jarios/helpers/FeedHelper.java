package local.jarios.helpers;

import local.jarios.common.util.*;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.enums.TipoConexion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.*;
import local.jarios.mappers.MapperFeed;
import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.services.Service;
import local.jarios.services.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.w3._2005.atom.FeedType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class FeedHelper {

    private FeedHelper() { }

    // ==========================
    // MÉTODOS PÚBLICOS
    // ==========================

    /**
     * Obtiene la lista de feeds parseados desde local o remoto.
     */
    public static List<Feed> parsearFeeds(Log miLog, Feed newestFeed, Estadistica estadistica, boolean isLocal)
            throws MiParseException {

        log.info("[parsearFeeds] - Inicio el parseo de los Feeds.");
        List<Feed> listFeeds = new ArrayList<>();

        try {
            var unmarshaller = getValidUnmarshaller();
            String nextLink = getInitialLink(isLocal);
            log.debug("[parsearFeeds] - NextLink: {}", nextLink);

            boolean enBucle = true;

            while (enBucle) {
                try (var reader = openBufferedReader(nextLink, isLocal)) {

                    estadistica.aumentarNFicheros();
                    log.debug("[parsearFeeds] - Nº de fichero: {}", estadistica.getNFicheros());

                    var feedType = getFeedType(unmarshaller, reader);
                    if (feedType == null) {
                        log.debug("[parseFeeds] - FeedType es null.");
                        break;
                    }

                    var feed = MapperFeed.getFeed(miLog, feedType);
                    log.debug("[parseFeeds] - Parseado correctamente {}", feed);

                    if (isLocal || isFechaValida(feed.getUpdated(), newestFeed)) {

                        log.info(Mensajes.FEED_INFO, nextLink, Mensajes.FEED_FECHAS_OK);
                        procesarFeed(feed, estadistica);
                        listFeeds.add(feed);
                        ComunHelper.imprimir(feed);

                        nextLink = getNextLink(feed, isLocal);
                        enBucle = isNextLinkValid(nextLink, isLocal);
                    } else {
                        log.info(Mensajes.FEED_INFO, nextLink, Mensajes.FEED_FECHAS_NO_OK);
                        enBucle = false;
                    }
                }
            }

            return listFeeds;

        } catch (IOException | URISyntaxException | MiInvalidDateFormatException |
                 PropertiesManagerException | MiUnmarshallerException | JAXBException ex) {
            throw new MiParseException(ex);
        }
    }

    /**
     * Devuelve el feed más reciente en base al tipo de sindicación.
     */
    public static Feed getNewestFeed(TipoSindicacion tipoSindicacion) throws MiServiceException {
        Service service = new ServiceImpl(TipoConexion.PRINCIPAL);
        log.info(Mensajes.SERVICE_CREACION_CREADO, TipoConexion.PRINCIPAL);
        return service.getNewestFeed(tipoSindicacion);
    }

    // ==========================
    // MÉTODOS PRIVADOS
    // ==========================

    private static void procesarFeed(Feed feed, Estadistica estadistica) throws MiInvalidDateFormatException {
        List<Entry> entries = feed.getListEntry();
        estadistica.setNEntryLeidos(estadistica.getNEntryLeidos() + entries.size());

        for (Entry entry : entries) {
            EntryHelper.procesarEntry(entry, estadistica);
        }
    }

    @SuppressWarnings("unchecked")
    private static FeedType getFeedType(Unmarshaller unmarshaller, BufferedReader reader) throws JAXBException {
        return ((JAXBElement<FeedType>) unmarshaller.unmarshal(reader)).getValue();
    }

    private static String getInitialLink(boolean isLocal) throws PropertiesManagerException, URISyntaxException {
        if (isLocal) {
            String filename = PropertiesHelper.getProperty(Constantes.APP_PROPERTIES, PropertiesKeys.APP_FILENAME);
            Path resolved = getPathBaseLocal().resolve(filename).normalize();

            if (!resolved.startsWith(getPathBaseLocal())) {
                throw new PropertiesManagerException("Ruta inicial no permitida: " + resolved);
            }

            return resolved.toString();
        } else {
            String url = PropertiesHelper.getProperty(Constantes.APP_PROPERTIES, PropertiesKeys.APP_URL);
            UrlHelper.validateRemoteUrl(url);
            return url;
        }
    }

    private static boolean isNextLinkValid(String nextLink, boolean isLocal) throws PropertiesManagerException, URISyntaxException {
        return isLocal ? FileHelper.esFileValido(nextLink) : UrlHelper.esUrlValida(nextLink);
    }

    private static String getNextLink(Feed feed, boolean isLocal) throws PropertiesManagerException, URISyntaxException {
        if (isLocal) {
            Path resolved = getPathBaseLocal().resolve(feed.getLinkNext()).normalize();
            if (!resolved.startsWith(getPathBaseLocal())) {
                throw new PropertiesManagerException("Ruta de archivo local no permitida: " + resolved);
            }
            return resolved.toString();
        } else {
            String url = feed.getLinkNext();
            UrlHelper.validateRemoteUrl(url);
            return url;
        }
    }

    private static Unmarshaller getValidUnmarshaller() throws MiUnmarshallerException {
        var unmarshaller = UnmarshallerHelper.getUnmarshaller();
        if (unmarshaller == null) {
            throw new MiUnmarshallerException("Unmarshaller nulo. No se puede continuar.");
        }
        return unmarshaller;
    }

    private static BufferedReader openBufferedReader(String pathOrUrl, boolean isLocal) throws IOException, URISyntaxException {
        InputStream stream = isLocal
                ? new FileInputStream(pathOrUrl)
                : new URI(pathOrUrl).toURL().openStream();

        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
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

    private static Path getPathBaseLocal() throws PropertiesManagerException {
        return Paths
                .get(PropertiesHelper.getProperty(Constantes.APP_PROPERTIES, PropertiesKeys.APP_PATH))
                .toAbsolutePath()
                .normalize();
    }
}
