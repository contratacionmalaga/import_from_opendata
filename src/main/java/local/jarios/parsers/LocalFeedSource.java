package local.jarios.parsers;

import local.jarios.common.util.PropertiesFiles;
import local.jarios.common.util.PropertiesKeys;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.FileHelper;
import local.jarios.helpers.PropertiesHelper;
import local.jarios.interfaces.FeedSource;
import local.jarios.properties.exception.PropertiesManagerException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Description:
 * Author: juan
 * Date: 04/07/2025
 * Team:
 */
public class LocalFeedSource implements FeedSource {
    @Override
    public String getInitialLink() throws Exception {
        String filename = PropertiesHelper.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_FILENAME);
        Path resolved = getPathBaseLocal().resolve(filename).normalize();
        if (!resolved.startsWith(getPathBaseLocal())) throw new Exception("Ruta no permitida");
        return resolved.toString();
    }

    @Override
    public boolean isNextLinkValid(String link) {
        return FileHelper.esFileValido(link);
    }

    @Override
    public String getNextLink(Feed feed) throws Exception {
        Path resolved = getPathBaseLocal().resolve(feed.getLinkNext()).normalize();
        if (!resolved.startsWith(getPathBaseLocal())) throw new Exception("Ruta no permitida");
        return resolved.toString();
    }

    @Override
    public BufferedReader openBufferedReader(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
    }

    private Path getPathBaseLocal() throws PropertiesManagerException {
        return Paths.get(PropertiesHelper.getProperty(PropertiesFiles.APP, PropertiesKeys.APP_PATH))
                .toAbsolutePath()
                .normalize();
    }
}
