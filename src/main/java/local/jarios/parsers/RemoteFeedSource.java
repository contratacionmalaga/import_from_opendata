package local.jarios.parsers;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.PropertiesHelper;
import local.jarios.helpers.UrlHelper;
import local.jarios.interfaces.FeedSource;
import local.jarios.properties.exception.PropertiesManagerException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * Description:
 * Author: juan
 * Date: 04/07/2025
 * Team:
 */
@Slf4j
public class RemoteFeedSource implements FeedSource {
    @Override
    public String getInitialLink() throws Exception {
        String url = PropertiesHelper.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_URL);
        log.debug("[getInitialLink] - Url: {}", url);
        UrlHelper.validateRemoteUrl(url);
        log.debug("[getInitialLink] - Url válida.");
        return url;
    }

    @Override
    public boolean isNextLinkValid(String link) throws PropertiesManagerException, URISyntaxException  {
        boolean esValida = UrlHelper.esUrlValida(link);
        log.debug("[isNextLinkValid] - ¿Es válida la URL: '{}'? {}", link, esValida);
        return esValida;
    }

    @Override
    public String getNextLink(Feed feed) throws Exception {
        String url = feed.getLinkNext();
        log.debug("[getNextLink] - LinkNext asociado al Feed '{}': {}", feed, url);
        UrlHelper.validateRemoteUrl(url);
        log.debug("[getNextLink] - La Url es válida.");
        return url;
    }

    @Override
    public BufferedReader openBufferedReader(String path) throws Exception {
        BufferedReader bufferedReader =
                new BufferedReader
                        (new InputStreamReader(
                                new URI(path).toURL().openStream(), StandardCharsets.UTF_8));
        log.debug("[openBufferedReader] - Creado un BufferedReader para la ruta: {}", path);
        return bufferedReader;
    }
}
