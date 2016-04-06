package net.onenandone.fralax;

/**
 * @author Daniel Draper Johann Bähler
 *         Created on 05.04.16.
 * @version 1.0
 *          A simple XmlParser that supports loading ina file parsing it and supports further searches on the returned DOM (as an XmlContext).
 */
public interface XmlParser {

    /**
     * Returns the parsed file after running it through the parser.
     *
     * @param file file to parse.
     * @return parsed xml as xml Context.
     */
    XmlContext parse(final String file);

}
