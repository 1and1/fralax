package net.onenandone.fralax;

import net.onenandone.fralax.parser.VtdXmlParser;

import java.util.Objects;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 06.04.16.
 * @version 1.0
 * Factory used to create the default (Vtd) XMLParser or a specific parser.
 * REQUIRES Parser Implementation to have default constructor!
 */
public class Fralax {

    public static XmlContext parse (final String file) {
        return parse(file, VtdXmlParser.class);
    }

    public static XmlContext parse(final String file, final Class<? extends XmlParser> xmlParserClass) {
        Objects.requireNonNull(file, "the xml file may not be null");
        Objects.requireNonNull(xmlParserClass, "the xml parser class may not be null");

        try {
            final XmlParser xmlParser = xmlParserClass.newInstance();
            return xmlParser.parse(file);
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new FralaxException("could not instantiate xml parser class '" + xmlParserClass.getCanonicalName() + "'", e);
        }
    }

}
