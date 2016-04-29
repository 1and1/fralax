package net.onenandone.fralax;

import com.greenbird.xml.prettyprinter.PrettyPrinter;
import com.greenbird.xml.prettyprinter.PrettyPrinterBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Represents a parsed XmlFile or result of an executed XPath-Query.
 */
public interface XmlContext {

    /**
     * Searches for an XPathQuery and returns the result as an XmlContext if it exists{@link Optional#empty()} otherwise.
     * E.g.
     * <pre>
     * {@code
     * XmlContext xml = Fralax.parse(fileToParse);
     * XmlContext objectRR1 = xml.select("//vehicle[@id='RR1']"); //returns new XmlContext
     * objectRR1.select("/vehicle-id"); //returns the vehicle-id object only for the previously selected vehicle object
     * }
     * </pre>
     *
     * @param xpath the xpath query to search for.
     * @return a new XmlContext that can be parsed using xpath again.
     * @throws FralaxException thrown when an error occurs during parsing.
     */
    Optional<XmlContext> select(final String xpath) throws FralaxException;

    /**
     * Searches for an XPathQuery and returns the result as a list of XmlContexts that again are parsable with xpath.
     * E.g.
     * <pre>
     * {@code
     * XmlContext xml = Fralax.parse(fileToParse);
     * List\<XmlContext> vehicles = xml.selectAll("//vehicle"); //returns new list of vehicles as parsed XmlContexts.
     * vehicles.get(0).select("/vehicle-id"); //returns the vehicle-id object only for the first element of the previously selected vehicles.
     * }
     * </pre>
     *
     * @param xpath the xpath query to search for.
     * @return a new XmlContext that can be parsed using xpath again.
     * @throws FralaxException thrown when an error occurs during parsing.
     */
    List<XmlContext> selectAll(final String xpath) throws FralaxException;

    /**
     * Returns object as unformatted String (no indentation/line-breaks).
     *
     * @return object as unformatted String.
     */
    String asString();

    /**
     * Returns object as unformatted String if {@code formatted} is set to {@code false} ({@link #asString()})
     * or the object correctly indented and with correct line-breaks otherwise.
     *
     * @param formatted boolean flag indicating if output should be formatted
     * @return object as String.
     */
    default String asString(final boolean formatted) {
        String unformatted = asString();
        if (formatted) {
            PrettyPrinter prettyPrinter = PrettyPrinterBuilder.newPrettyPrinter().ignoreWhitespace().build();
            StringBuilder buffer = new StringBuilder();
            if (prettyPrinter.process(unformatted, buffer)) {
                String formattedString = buffer.toString();
                if (formattedString.startsWith("\n")) {
                    formattedString = formattedString.substring(1, formattedString.length());
                    return formattedString;
                }
            } else {
                throw new FralaxException("Error when pretty printing the xml file " + unformatted);
            }
        }
        return unformatted;
    }

    /**
     * An optional method that will return if the underlying document has been modified since the first parse. Will return true if
     * the document was not modified and false if it was. In the case of in-place parsers like SAX or StAX, this method
     * is not necessarily needed, as continued parses on a file will re-read the entire document. However, it
     * is preferred to copy and save the original file and use this for continued parsing as to not cause the following
     * case:
     * <pre>
     * {@code
     * XmlContext xml = Fralax.parse(fileToParse);
     * xml.select("//element1");
     * //Now the underlying xml file is changed externally removing all content.
     * xml.select("//elementThatIsChildOfElement1"); //Will throw a FralaxException in in-place parsers as the xml file is now empty.
     * }
     * </pre>
     * In the case of in-memory parsers, (like the default VtdParser) this method should always be implemented as to give the user a chance to
     * check for changes in the original document and reparse the document if needed.
     *
     * @return if the underlying document has been modified externally.
     */
    default boolean isValid() {
        return true;
    }
}
