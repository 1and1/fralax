package net.onenandone.fralax.parser;

import java.util.Map;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 */
public interface XMLParserFactory {

    /**
     * @param file The file to watch and parse
     * @param namespaces A map of namespace prefixed and uris
     * @return a XMLParser
     */
    XMLParser createNewInstance(String file, Map<String, String> namespaces);
}
