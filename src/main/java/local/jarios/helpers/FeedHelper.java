package local.jarios.helpers;

import local.jarios.common.util.PropertiesKeys;
import local.jarios.common.util.VariablesGlobales;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.enums.TipoConexion;
import local.jarios.enums.TipoSindicacion;
import local.jarios.exceptions.*;
import local.jarios.mappers.MapperFeed;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.Mensajes;
import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.services.Service;
import local.jarios.services.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.w3._2005.atom.FeedType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Juan Antonio
 */
@Slf4j
public final class FeedHelper {

    private FeedHelper() { }

    /**
     * Función encargada de devolver la lista de Entry que cumplen las condiciones establecidas.
     * Realiza el análisis para ficheros ubicados en LOCAL
     *
     * @param feed Objeto JAVA que será analizado para la obtención de la lista de Entry
     */
    private static void procesarFeed(Feed feed, Estadistica estadistica) throws MiInvalidDateFormatException {

        //
        List<Entry> entries = feed.getListEntry();
        log.debug("[procesarFeed] - Leídos los Entry asociados al Feed. Nº: {}, Lista: {}", entries.size(), entries);

        // Compruebo si la lista de Entry asociada al Feed es NO VACÍA
        if (!entries.isEmpty()) {

            // Aumento el número de entries leídos
            estadistica.setNEntryLeidos(estadistica.getNEntryLeidos() + entries.size());

            // Itero sobre cada Entry en la lista del Feed
            for (Entry entry : entries) {

                //
                EntryHelper.procesarEntry(entry, estadistica);
            }

        }
    }

    /**
     * Función encargada de obtener un objeto FeedType según la norma dictaminada por w3 2005 sobre ficheros ATOM
     *
     * @param unmarshaller Lógica para generar el objeto FeedType a partir del ATOM
     * @param bufferedReader Canal de lectura sobre el fichero
     *
     * @return FeedType Objeto con el formato definido por la w3 2005 sobre los ficheros ATOM. En caso de producirse
     *                  una excepción devuelve NULL.
     */
    private static FeedType getFeedType(Unmarshaller unmarshaller, BufferedReader bufferedReader)
            throws JAXBException {

        //  Obtengo el FeedType asociado a un fichero ATOM
        return ((JAXBElement<FeedType>) unmarshaller.unmarshal(bufferedReader)).getValue();

    }

    /**
     *
     * @param miLog Objeto que representa el Log con los datos de la ejecución actual del programa
     *
     * @return List<Feed> Lista de los elementos Feed asociados a la importación Log
     * @throws MiParseException Excepción que se lanza cuando existe un error en el parseo
     */
    public static List<Feed> parsearFeeds(Log miLog, Feed newestFeed, Estadistica estadistica, boolean isLocal)
            throws MiParseException {

        log.info("[parsearFeeds] - Inicio el parseo de los Feeds.");

        List<Feed> listFeeds = new ArrayList<>();

        try {
            var unmarshaller = getValidUnmarshaller();

            String nextLink = getInitialLink(isLocal);
            log.debug("[parsearFeeds] - NextLink: {}", nextLink);

            var fechaHoraInicialLectura = VariablesGlobales.getFiltroFechaInicial();
            log.debug("[parsearFeeds] - fechaHoraInicialLectura: {}", fechaHoraInicialLectura);

            var fechaHoraFinalLectura = VariablesGlobales.getFiltroFechaFinal();
            log.debug("[parsearFeeds] - fechaHoraFinalLectura: {}", fechaHoraFinalLectura);

            boolean enBubcle = true;

            while (enBubcle) {

                try (
                        var bufferedReader = openBufferedReader(nextLink, isLocal)
                ) {

                    estadistica.aumentarNFicheros();
                    log.debug("[parsearFeeds] - Nº de fichero: {}", estadistica.getNFicheros());

                    var feedType = getFeedType(unmarshaller, bufferedReader);
                    log.debug("[parsearFeeds] - Obtenido el FeedType correspondientes asociado al fichero.");

                    if (feedType == null) {
                        log.debug("[parseFeeds] - El FeedType es null.");
                        break;
                    }

                    var feed = MapperFeed.getFeed(miLog, feedType);
                    log.debug("[parseFeeds] - Parseado correctamente {}", feed);

                    if ((isLocal) || (isFechaValida(feed.getUpdated(), newestFeed))) {

                        log.info(Mensajes.FEED_INFO, nextLink, Mensajes.FEED_FECHAS_OK);

                        procesarFeed(feed, estadistica);
                        log.info("[parsearFeeds] - {} parseado corectamente.", feed);

                        listFeeds.add(feed);
                        log.info("[parsearFeeds] - Tamaño de ListFeed<Feed>: {}", listFeeds.size());

                        ComunHelper.imprimir(feed);

                        nextLink = getNextLink(feed, isLocal);
                        log.info("NextLink: {}", nextLink);

                        enBubcle = isNextLinkValid(nextLink, isLocal);

                    } else {

                        log.info(Mensajes.FEED_INFO, nextLink, Mensajes.FEED_FECHAS_NO_OK);

                        if (log.isDebugEnabled()) {
                            log.info(Mensajes.FECHAS_FEED_LOCAL,
                                    Constantes.TABULADOR_1,
                                    feed.getUpdated(),
                                    fechaHoraFinalLectura);
                        }
                        enBubcle = false;
                    }
                }
            }

            return listFeeds;

        } catch (IOException | URISyntaxException | MiInvalidDateFormatException | PropertiesManagerException |
                 MiUnmarshallerException | JAXBException ex) {

            throw new MiParseException(ex);
        }
    }

    private static String getInitialLink(boolean isLocal) throws PropertiesManagerException, URISyntaxException {

        if (isLocal) {
            String filename = PropertiesHelper.getProperty(Constantes.APP_PROPERTIES, PropertiesKeys.APP_FILENAME);
            log.debug("[getInitialLink] - Importando desde Local. Link inicial: {}", filename);
            Path resolvedPath = getPathBaseLocal().resolve(filename).normalize();
            log.debug("[getInitialLink] - Path absoluto: {}", resolvedPath);

            if (!resolvedPath.startsWith(getPathBaseLocal())) {
                throw new PropertiesManagerException("Ruta inicial no permitida: " + resolvedPath);
            }

            return resolvedPath.toString();
        } else {
            String url = PropertiesHelper.getProperty(Constantes.APP_PROPERTIES, PropertiesKeys.APP_URL);
            UrlHelper.validateRemoteUrl(url);
            return url;
        }
    }

    private static boolean isNextLinkValid(String nextLink, boolean isLocal) throws PropertiesManagerException, URISyntaxException {
        if (isLocal) {
            return FileHelper.esFileValido(nextLink);
        } else {
            return UrlHelper.esUrlValida(nextLink);
        }
    }

    private static String getNextLink(Feed feed, boolean isLocal) throws PropertiesManagerException, URISyntaxException {
        if (isLocal) {
            Path resolvedPath = getPathBaseLocal().resolve(feed.getLinkNext()).normalize();
            if (!resolvedPath.startsWith(getPathBaseLocal())) {
                throw new PropertiesManagerException("Ruta de archivo local no permitida: " + resolvedPath);
            }
            return resolvedPath.toString();
        } else {
            String url = feed.getLinkNext();
            UrlHelper.validateRemoteUrl(url);  // Esto lo defines abajo
            return url;
        }
    }



    private static Unmarshaller getValidUnmarshaller() throws MiUnmarshallerException {
        var unmarshaller = UnmarshallerHelper.getUnmarshaller();
        if (unmarshaller == null) {
            throw new MiUnmarshallerException("El Unmarshaller es null, no se puede continuar con el parsing.");
        }
        return unmarshaller;
    }

    private static BufferedReader openBufferedReader(String nextLink, boolean isLocal) throws IOException, URISyntaxException {
        if (isLocal) {
            return new BufferedReader(new InputStreamReader(new FileInputStream(nextLink), StandardCharsets.UTF_8));
        } else {
            return new BufferedReader(new InputStreamReader(new URI(nextLink).toURL().openStream(), StandardCharsets.UTF_8));
        }
    }

    private static boolean isFechaValida(LocalDateTime updatedFeed, Feed newestFeed) {

        // La fecha updatedFeed es válida si:
        // - newestFeed es NULL
        // - Es posterior a newestFeed.getUpdated()
        // - Está entre VariablesGlobales.filtroFechaInicial y VariablesGlobales.filtroFechaFinal
        //   (ambos extremos incluidos o excluidos, según lo que prefieras)

        boolean esFechaValida = false;

        // Verificar si newestFeed es no nulo
        if (newestFeed != null) {
            esFechaValida = updatedFeed != null
                    && newestFeed.getUpdated() != null
                    && updatedFeed.isAfter(newestFeed.getUpdated()) // La fecha debe ser posterior a newestFeed.getUpdated()
                    && estaDentroDelRango(updatedFeed); // Verifica si está dentro del rango de fechas

            log.debug("[isFechaValida] - Fecha: {}, FechaUpdatedNewestFeed: {}, FechaInicial: {}, FechaFinal: {}",
                    updatedFeed,
                    newestFeed.getUpdated(),
                    VariablesGlobales.getFiltroFechaInicial(),
                    VariablesGlobales.getFiltroFechaFinal());
        } else {
            // Si newestFeed es nulo, solo validamos el rango de fechas
            esFechaValida = updatedFeed != null && estaDentroDelRango(updatedFeed);

            log.debug("[isFechaValida] - Fecha: {}, FechaInicial: {}, FechaFinal: {}",
                    updatedFeed,
                    VariablesGlobales.getFiltroFechaInicial(),
                    VariablesGlobales.getFiltroFechaFinal());
        }

        return esFechaValida;
    }

    private static Path getPathBaseLocal() throws PropertiesManagerException {

        return Paths
                .get(PropertiesHelper.getProperty(Constantes.APP_PROPERTIES, PropertiesKeys.APP_PATH))
                .toAbsolutePath()
                .normalize();
    }

    public static Feed getNewestFeed(TipoSindicacion tipoSindicacion) throws MiServiceException {

        Service service = new ServiceImpl(TipoConexion.PRINCIPAL);
        log.info(Mensajes.SERVICE_CREACION_CREADO, TipoConexion.PRINCIPAL);

        return service.getNewestFeed(tipoSindicacion);
    }

    // Método auxiliar para validar si la fecha está dentro del rango
    private static boolean estaDentroDelRango(LocalDateTime updatedFeed) {
        boolean dentroFechaInicial = VariablesGlobales.getFiltroFechaInicial() == null || !updatedFeed.isBefore(VariablesGlobales.getFiltroFechaInicial());
        boolean dentroFechaFinal = VariablesGlobales.getFiltroFechaFinal() == null || !updatedFeed.isAfter(VariablesGlobales.getFiltroFechaFinal());

        return dentroFechaInicial && dentroFechaFinal;
    }
}
