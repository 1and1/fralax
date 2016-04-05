package net.onenandone.fralax.parser;

import lombok.Getter;
import lombok.extern.java.Log;
import net.onenandone.fralax.model.*;

import javax.xml.xpath.*;
import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 * The Wrapper Class used for easier XML XPath searching. Has protected submethods of the XPathSearch as to make implementation easier for different kinds of concrete Parsers.
 * @see XPathParser#searchFor(String xPathQuery)) for API Definitions.
 */
@Log
public abstract class XMLParser implements XPathParser {

    @Getter
    private final File fileToParse;

    @Getter
    private Map mapOfNamespaces;

    /**
     * Only used by subclasses to create superclass in constructor.
     * @param fileToParse the file used for XML Parsing.
     * @param namespaces the namespace definitions that can then be referenced in XpathQueries. e.g. ("sn" "www.w3.org")
     */
    protected XMLParser(File fileToParse, Map<String, String> namespaces) {
        this.fileToParse = fileToParse;
        this.mapOfNamespaces = namespaces;
    }

    /**
     * Method to searchForAllAttributes of a certain kind (e.g. xpath="@id"). Has to be implemented by concrete subclass.
     * @param xPathQuery the Query to search for.
     * @return ListOfXMLAttributes
     */
    protected abstract ListOfXMLAttributes searchForAllAttributes(final String xPathQuery);


    /**
     * Method to searchForASingleAttribute of a certain kind (e.g. xpath="//driver[@id='123']/@id"). Has to be implemented by concrete subclass.
     * @param xPathQuery the Query to search for.
     * @return Single XMLAttribute
     */
    protected abstract XMLAttribute searchForAttribute(final String xPathQuery);

    protected abstract XMLElement searchForElement(final String xPathQuery);

    protected abstract ListOfXMLElements searchForAllElements(final String xPathQuery);


    /**
     * Main API method used to access XML File. Requires Correct XPath Syntax. To Access use
     * <code>XPathParser parser = XMLParserDefaultFactory.createNewInstance(String file, Map<String, String> namespaces);
     *       Optional<XPathResult> result = parser.searchFor("//driver[@name='fralax'];
     *       if (result.isPresent()) {
     *           XPathResult result = result.get();
     *           if (XMLElement.class.isAssignableFrom(result.getClass()) {
     *              (XMLElement)result.doSomething();
     *              }
     *       }</code>
     * @see <a href="http://www.w3schools.com/xsl/xpath_syntax.asp">http://www.w3schools.com/xsl/xpath_syntax.asp</a>
     * @param xPathQuery the Query to search for.
     * @return an #Optional object.
     * @throws XPathException when the XPath doesn't compile
     */
    public Optional<XPathResult> searchFor(String xPathQuery) throws XPathException {
        switch (evaluateXPath(xPathQuery)) {
            case ATTRIBUTE: {
                XMLAttribute result = searchForAttribute(xPathQuery);
                return result != null ? Optional.of(result) : Optional.empty();
            }
            case MULTIPLE_ATTRIBUTES: {
                ListOfXMLAttributes result = searchForAllAttributes(xPathQuery);
                return (result != null && !(result.getAttributeList() == null) && !result.getAttributeList().isEmpty()) ? Optional.of(result) : Optional.empty();
            }
            case ELEMENT: {
                XMLElement result = searchForElement(xPathQuery);
                return result != null ? Optional.of(result) : Optional.empty();
            }
            case MULTIPLE_ELEMENTS: {
                ListOfXMLElements result = searchForAllElements(xPathQuery);
                return result != null && !(result.getElementList() == null) && !result.getElementList().isEmpty() ? Optional.of(result) : Optional.empty();
            }
            default: {
                log.log(Level.WARNING, "Error parsing the XPath Expression, evaluate Namespace and/or XPath Syntax");
                throw new XPathExpressionException("Error parsing the XPath Expression, evaluate Namespace and/or XPath Syntax");
            }
        }
    }


    /**
     * Method to determine what kind of Request the XPath is as to delegate search to smaller methods.
     * @param xPathQuery the Query to evaluate
     * @return XPathType of the Query.
     */
    public static XPathType evaluateXPath(final String xPathQuery) {
        if (xPathQuery == null || xPathQuery.isEmpty()) {
            return XPathType.INVALID;
        }
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            xPath.compile(xPathQuery);
        } catch (XPathExpressionException e) {
            return XPathType.INVALID;
        }
        String[] parts = xPathQuery.split("/");
        boolean hasAttributeQuery = false;
        boolean hasSingleQuery = false;
        boolean hasSpecifyingQuery = false;
        boolean hasSingleNoSpecifying = false;
        int curIndex = 0;
        for (String s : parts) {
            curIndex++;
            if (s.contains("@")) {
                hasAttributeQuery = true;
            }
            if (s.contains("=") && s.contains("[") && curIndex == parts.length) {
                hasSingleQuery = true;
            }
            if (s.contains("[")) {
                hasSpecifyingQuery = true;
            }
            if (s.contains("=")) {
                hasSingleNoSpecifying = true;
            }
        }
        if (!hasAttributeQuery) {
            return XPathType.MULTIPLE_ELEMENTS;
        } else {
            if (!hasSingleQuery) {
                if (!hasSpecifyingQuery) {
                    if (!hasSingleNoSpecifying) {
                        return XPathType.MULTIPLE_ATTRIBUTES;
                    }
                    else {
                        return XPathType.ATTRIBUTE;
                    }
                }
                else {
                    return XPathType.MULTIPLE_ELEMENTS;
                }
            } else {
                return XPathType.ELEMENT;
            }
        }
    }

}
