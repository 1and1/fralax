package net.onenandone.fralax;

import net.onenandone.fralax.parser.VtdXmlParser;

import java.util.Objects;

/**
 * Factory used to create the default (Vtd) XMLParser or a specific parser.
 * Requirement for the static factory function to work for new Implementations: Default constructor must be visible.
 */
@SuppressWarnings("WeakerAccess")
public class Fralax {

    /**
     * @param file file to parse.
     * @return a new searchable XmlContext of the contents of the file if parsing was successful.
     * @see #parse(String, Class)
     */
    public static XmlContext parse(final String file) {
        return parse(file, VtdXmlParser.class);
    }


    /**
     * Creates a new FraLaX-API-fitting XmlParser with the passed file and parserClass.
     * Requires passed xmlParserClass to implement and have visible default constructor.
     *
     * @param file           file to parse.
     * @param xmlParserClass the class of the specific parser to create.
     * @return a new searchable XmlContext of the contents of the file if parsing was successful.
     */
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

    /**
     * Returns a managed context if {@code managed} is set to {@code true}, otherwise the same context {@link #parse(String, Class)} would return.
     *
     * @param file           file to parse.
     * @param xmlParserClass the class of the specific parser to create.
     * @param managed        boolean flag indicating whether the returned context should be kept up to date
     * @return a managed context if {@code managed} is set to {@code true}, otherwise the same context {@link #parse(String, Class)} would return
     */
    public static XmlContext parse(final String file, final Class<? extends XmlParser> xmlParserClass, final boolean managed) {
        if (managed) {
            return new ManagedXmlContext(file, xmlParserClass);
        }
        return parse(file, xmlParserClass);
    }

}
