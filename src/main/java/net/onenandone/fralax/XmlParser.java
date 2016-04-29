package net.onenandone.fralax;

/**
 * A simple XmlParser that supports loading ina file parsing it and supports further searches on the returned DOM (as an XmlContext).
 */
public interface XmlParser {

    /**
     * Returns the parsed file after running it through the parser.
     *
     * @param file file to parse.
     * @param autoUpdate if an autoUpdate should take place on external modification of the file.
     * @return parsed xml as xml Context.
     */
    XmlRootContext parse(final String file, boolean autoUpdate);

}
