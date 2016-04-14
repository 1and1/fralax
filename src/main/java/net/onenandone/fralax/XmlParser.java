package net.onenandone.fralax;

/**
 * A simple XmlParser that supports loading ina file parsing it and supports further searches on the returned DOM (as an XmlContext).
 *
 * @author Daniel Draper Johann Bähler
 * @version 1.0
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
