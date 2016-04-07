package net.onenandone.fralax;

import java.util.List;
import java.util.Optional;

/**
 * @author Daniel Draper Johann Böhler
 *         Created on 06.04.16.
 * @version 1.0
 *          Represents a parsed XmlFile or result of an executed XPath-Query.
 */
public interface XmlContext {

    /**
     * Registers a namespace with this certain xml document for use when evaluating xpath requests.
     * E.g. <code>XmlContext xml = Fralax.parse(fileToParse)
     * xml.registerNamespace("ns", "http://www.google.com")
     * xml.select("//ns:element") //now uses the namespace
     * </code>
     *
     * @param key   the key to register for the namespace.
     * @param value namespace value to register.
     */
    void registerNamespace(final String key, final String value);

    /**
     * Searches for an XPathQuery and returns the result as an XmlContext if it exists{@link Optional#empty()} otherwise.
     * E.g. <code>XmlContext xml = Fralax.parse(fileToParse)
     * XmlContext objectRR1 = xml.select("//vehicle[@id='RR1']") //returns new XmlContext
     * objectRR1.select("/vehicle-id") //returns the vehicle-id object only for the previously selected vehicle object
     * </code>
     *
     * @param xpath the xpath query to search for.
     * @return a new XmlContext that can be parsed using xpath again.
     * @throws FralaxException thrown when an error occurs during parsing.
     */
    Optional<XmlContext> select(final String xpath) throws FralaxException;

    /**
     * Searches for an XPathQuery and returns the result as a list of XmlContexts that again are parsable with xpath.
     * E.g. <code>XmlContext xml = Fralax.parse(fileToParse)
     * List\<XmlContext> vehicles = xml.selectAll("//vehicle") //returns new list of vehicles as parsed XmlContexts.
     * vehicles.get(0).select("/vehicle-id") //returns the vehicle-id object only for the first element of the previously selected vehicles.
     * </code>
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
     * Returns object correctly indented and with correct line-breaks. Same appearance as original xml.
     *
     * @return object as formatted String.
     */
    String asFormattedString();
}
