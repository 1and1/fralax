package net.onenandone.fralax;

import net.onenandone.fralax.parser.VtdXmlParser;

import java.util.Objects;

/**
 * Factory used to create the default (Vtd) XMLParser or a specific parser.
 * Requirement for the static factory function to work for new Implementations: Default constructor must be visible.
 */
@SuppressWarnings("WeakerAccess")
public class Fralax {

    public static FileWatcherThread fileWatcher;

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
        fileWatcher= new FileWatcherThread(file);
        fileWatcher.start();
        try {
            final XmlParser xmlParser = xmlParserClass.newInstance();
            return xmlParser.parse(file);
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new FralaxException("could not instantiate xml parser class '" + xmlParserClass.getCanonicalName() + "'", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("../resources/abc/de/xdfsada.books-qualified.xml".replaceFirst("/(.[^/]*)\\.xml", ""));
        System.out.println("../resources/abd.def".replaceFirst("/([^./]*)\\.([^/.]*)", ""));
    }
}