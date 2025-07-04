package local.jarios.interfaces;

import local.jarios.entity.atom.Feed;

import java.io.BufferedReader;

/**
 * Interfaz genérica para asegurar que los objetos tengan los métodos necesarios
 */
public interface FeedSource {

    // Método que devuelve el valor único del objeto, usado como clave
    String getInitialLink() throws Exception;
    boolean isNextLinkValid(String link) throws Exception;
    String getNextLink(Feed feed) throws Exception;
    BufferedReader openBufferedReader(String path) throws Exception;
}
