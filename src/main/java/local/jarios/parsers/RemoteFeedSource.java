package local.jarios.parsers;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.PropertiesHelper;
import local.jarios.helpers.UrlHelper;
import local.jarios.interfaces.FeedSource;
import local.jarios.properties.exception.PropertiesManagerException;

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
public class RemoteFeedSource implements FeedSource {
    @Override
    public String getInitialLink() throws Exception {
        String url = PropertiesHelper.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_URL);
        UrlHelper.validateRemoteUrl(url);
        return url;
    }

    @Override
    public boolean isNextLinkValid(String link) throws PropertiesManagerException, URISyntaxException  {
        return UrlHelper.esUrlValida(link);
    }

    @Override
    public String getNextLink(Feed feed) throws Exception {
        String url = feed.getLinkNext();
        UrlHelper.validateRemoteUrl(url);
        return url;
    }

    @Override
    public BufferedReader openBufferedReader(String path) throws Exception {
        return new BufferedReader(new InputStreamReader(new URI(path).toURL().openStream(), StandardCharsets.UTF_8));
    }
}
