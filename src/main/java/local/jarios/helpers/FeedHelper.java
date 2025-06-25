package local.jarios.helpers;

import local.jarios.common.util.PropertiesKeys;
import local.jarios.entity.Log;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.enums.TipoFecha;
import local.jarios.exceptions.*;
import local.jarios.mappers.MapperFeed;
import local.jarios.properties.api.PropertiesManagerService;
import local.jarios.properties.api.PropertiesManagerServiceImpl;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.Mensajes;
import local.jarios.properties.exception.PropertiesManagerException;
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
    private static void procesarFeed(Feed feed, Estadistica estadistica)
                throws MiInvalidDateFormatException {

        //
        List<Entry> entries = feed.getListEntry();

        // Compruebo si la lista de Entry asociada al Feed es NO VACÍA
        if (!entries.isEmpty()) {

            // Aumento el número de entries leídos
            estadistica.setNEntryLeidos(estadistica.getNEntryLeidos() + entries.size());

            // Itero sobre cada Entry en la lista del Feed
            for (Entry entry : entries) {

                //
                EntryHelper.procesarEntry(entry, estadistica);
            }

        } else {

            log.info("La lista de entries asociada al Feed ({}) se encuentra VACÍA.", feed.getLinkSelf());

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

        List<Feed> listFeedEntities = new ArrayList<>();

        try {
            var unmarshaller = getValidUnmarshaller();

            String nextLink = getInitialLink(isLocal);

            var fechaHoraFinalLectura = getFechaFinalLectura();
            var fechaHoraInicialLectura = getFechaInicialLectura();

            boolean salirBucle = false;

            while (!salirBucle && isNextLinkValid(nextLink, isLocal)) {

                try (
                        var bufferedReader = openBufferedReader(nextLink, isLocal)
                ) {

                    estadistica.aumentarNFicheros();

                    var feedType = getFeedType(unmarshaller, bufferedReader);

                    if (feedType == null) {
                        log.info(Mensajes.FEED_TYPE_NULL, Constantes.TABULADOR_2, true);
                        break;
                    }

                    var feed = MapperFeed.getFeed(miLog, feedType);

                    if (isFechaValida(feed, fechaHoraInicialLectura, fechaHoraFinalLectura, newestFeed, isLocal)) {

                        log.info(Mensajes.FEED_INFO, nextLink, Mensajes.FEED_FECHAS_OK);

                        procesarFeed(feed, estadistica);
                        listFeedEntities.add(feed);

                        nextLink = getNextLink(feed, isLocal);
                        log.info(Mensajes.NEXT_LINK, "", nextLink, isNextLinkValid(nextLink, isLocal));

                    } else {

                        log.info(Mensajes.FEED_INFO, nextLink, Mensajes.FEED_FECHAS_NO_OK);

                        if (log.isDebugEnabled()) {
                            log.info(Mensajes.FECHAS_FEED_LOCAL,
                                    Constantes.TABULADOR_1,
                                    feed.getUpdated(),
                                    fechaHoraFinalLectura);
                        }
                        salirBucle = true;
                    }
                }
            }

            return listFeedEntities;

        } catch (IOException | URISyntaxException | MiInvalidDateFormatException |
                 MiUnmarshallerException | JAXBException | MiUrlException ex) {

            throw new MiParseException(ex);
        }
    }

    private static String getInitialLink(boolean isLocal) throws PropertiesManagerException {

        PropertiesManagerService propertiesManagerService = PropertiesManagerServiceImpl.getInstance();

        try {
            if (isLocal) {
                String filename = propertiesManagerService.getProperty(Constantes.APP_PROPERTIES, Constantes.CONFIG_FILENAME);
                Path resolvedPath = getPathBaseLocal().resolve(filename).normalize();

                if (!resolvedPath.startsWith(getPathBaseLocal())) {
                    throw new PropertiesManagerException("Ruta inicial no permitida: " + resolvedPath);
                }

                return resolvedPath.toString();
            } else {
                String url = propertiesManagerService.getProperty(Constantes.APP_PROPERTIES, Constantes.CONFIG_URL);
                validateRemoteUrl(url);
                return url;
            }
        } catch (PropertiesManagerException ex) {
            log.error("[getInitialNextLink] - Error al leer desde Properties.");
            log.error(ex.getMessage());
            throw ex;
        }
    }

    private static boolean isNextLinkValid(String nextLink, boolean isLocal) throws MiUrlException {
        if (isLocal) {
            return FileHelper.esFileValido(nextLink);
        } else {
            return UrlHelper.esUrlValida(nextLink);
        }
    }

    private static String getNextLink(Feed feed, boolean isLocal) throws PropertiesManagerException {
        if (isLocal) {
            Path resolvedPath = getPathBaseLocal().resolve(feed.getLinkNext()).normalize();
            if (!resolvedPath.startsWith(getPathBaseLocal())) {
                throw new PropertiesManagerException("Ruta de archivo local no permitida: " + resolvedPath);
            }
            return resolvedPath.toString();
        } else {
            String url = feed.getLinkNext();
            validateRemoteUrl(url);  // Esto lo defines abajo
            return url;
        }
    }

    private static void validateRemoteUrl(String urlStr) throws PropertiesManagerException {

        log.debug("[validateRemoteUrl]: urlStr: {}", urlStr);

        PropertiesManagerService propertiesManagerService = PropertiesManagerServiceImpl.getInstance();
        String miScheme = propertiesManagerService.getProperty(Constantes.VALIDATION_PROPERTIES, Constantes.PARAMETRO_URI_SCHEME);
        log.debug("[validateRemoteUrl]: Scheme de validación: {}", miScheme);
        String miHost = propertiesManagerService.getProperty(Constantes.VALIDATION_PROPERTIES, Constantes.PARAMETRO_URI_HOST);
        log.debug("[validateRemoteUrl]: Host de validación: {}", miHost);
        String msg;
        //
        try {
            URI uri = new URI(urlStr);
            log.debug("[validateRemoteUrl] - Uri: {}", uri);
            String scheme = uri.getScheme();
            log.debug("[validateRemoteUrl] - Scheme: {}", scheme);
            String host = uri.getHost();
            log.debug("[validateRemoteUrl] - Host: {}", host);

            if (!miScheme.equalsIgnoreCase(scheme)) {
                msg = String.format ("[validateRemoteUrl] - Solo se permiten URLs HTTPS. Esquema encontrado: %s", scheme);
                log.error(msg);
                throw new PropertiesManagerException(msg);
            }

            // Restringe si quieres el host o dominio
            if (host == null || !host.endsWith(miHost)) {
                msg = String.format ("[validateRemoteUrl] - Solo se permiten URLs HTTPS. Esquema encontrado: %s", scheme);
                log.error(msg);
                throw new PropertiesManagerException("[validateRemoteUrl] - Host no permitido: " + host);
            }

        } catch (URISyntaxException ex) {
            msg = String.format ("[validateRemoteUrl] - URL remota inválida: %s", urlStr);
            log.error(msg, ex);
            throw new PropertiesManagerException(msg, ex);
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

    private static boolean isFechaValida(
            Feed feed,
            LocalDateTime fechaHoraInicialLectura,
            LocalDateTime fechaHoraFinalLectura,
            Feed newestFeed,
            boolean isLocal) {

        boolean fechaValida =
                (feed.getUpdated().isAfter(fechaHoraFinalLectura)) &&
                        (feed.getUpdated().isBefore(fechaHoraInicialLectura));

        if (!isLocal && newestFeed != null) {
            fechaValida = fechaValida && (feed.getUpdated().isAfter(newestFeed.getUpdated()));
        }
        return fechaValida;
    }

    /**
     *
     * @return Valor con la fecha donde comienza la importación de los datos
     */
    private static LocalDateTime getFechaInicialLectura() {

        //
        return getFechaFromProperty(PropertiesKeys.FILTRO_FECHAINICIALLECTURA);
    }

    /**
     *
     * @return Valor con la fecha donde finaliza la importación de los datos
     */
    private static LocalDateTime getFechaFinalLectura() {

        //
        return getFechaFromProperty(PropertiesKeys.FILTRO_FECHAFINALLECTURA);
    }



    private static Path getPathBaseLocal() {

        PropertiesManagerService propertiesManagerService = PropertiesManagerServiceImpl.getInstance();

        return Paths.get(
                propertiesManagerService
                        .getProperty(Constantes.APP_PROPERTIES, Constantes.CONFIG_PATH))
                        .toAbsolutePath()
                        .normalize();
    }
}
