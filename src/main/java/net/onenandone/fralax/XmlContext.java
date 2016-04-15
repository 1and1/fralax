package net.onenandone.fralax;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

/**
 * Represents a parsed XmlFile or result of an executed XPath-Query.
 */
public interface XmlContext {

    /**
     * Registers a namespace with this certain xml document for use when evaluating xpath requests.
     * E.g.
     *
     * XmlContext xml = Fralax.parse(fileToParse);
     * xml.registerNamespace("ns", "http://www.google.com");
     * xml.select("//ns:element"); //now uses the namespace
     * }
     * </pre>
     *
     * @param key   the key to register for the namespace.
     * @param value namespace value to register.
     */
    void registerNamespace(final String key, final String value);

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
        if (formatted) {
            try {
                final Source xmlInput = new StreamSource(new StringReader(asString()));
                final StringWriter stringWriter = new StringWriter();
                final StreamResult xmlOutput = new StreamResult(stringWriter);
                final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                transformerFactory.setAttribute("indent-number", 2);
                final Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.transform(xmlInput, xmlOutput);
                return xmlOutput.getWriter().toString();
            } catch (final TransformerException e) {
                throw new FralaxException("could not format string", e);
            }
        }
        return asString();
    }
}
