package net.onenandone.fralax.parser;

import lombok.Getter;
import net.onenandone.fralax.model.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 */
public abstract class XMLParser {

    @Getter
    private final File fileToParse;

    @Getter
    private Map mapOfNamespaces;

    protected XMLParser(File fileToParse, Map<String, String> namespaces) {
        this.fileToParse = fileToParse;
        this.mapOfNamespaces = namespaces;
    }

    protected abstract ListOfXMLAttributes searchForAllAttributes(final String xPathQuery);

    protected abstract XMLAttribute searchForAttribute(final String xPathQuery);

    protected abstract XMLElement searchForElement(final String xPathQuery);

    protected abstract ListOfXMLElements searchForAllElements(final String xPathQuery);

    public Optional<XPathResult> searchFor(String xPathQuery) throws XPathException {
        switch (evaluateXPath(xPathQuery)) {
            case ATTRIBUTE: {
                XMLAttribute result = searchForAttribute(xPathQuery);
                return result != null ? Optional.of(result) : null;
            }
            case MULTIPLE_ATTRIBUTES: {
                ListOfXMLAttributes result = searchForAllAttributes(xPathQuery);
                return (result != null && !(result.getAttributeList() == null) && !result.getAttributeList().isEmpty()) ? Optional.of(result) : null;
            }
            case ELEMENT: {
                XMLElement result = searchForElement(xPathQuery);
                return result != null ? Optional.of(result) : null;
            }
            case MULTIPLE_ELEMENTS: {
                ListOfXMLElements result = searchForAllElements(xPathQuery);
                return result != null && !(result.getElementList() == null) && !result.getElementList().isEmpty() ? Optional.of(result) : null;
            }
            default: {
                throw new XPathExpressionException("Error parsing the XPath Expression, evaluate Namespace and/or XPath Syntax");
            }
        }
    }

    private static XPathType evaluateXPath(final String xPathQuery) {
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
        for (String s : parts) {
            if (s.contains("@")) {
                hasAttributeQuery = true;
            }
            if (s.contains("=") && s.contains("[")) {
                hasSingleQuery = true;
            }
            if (s.contains("[")) {
                hasSpecifyingQuery = true;
            }
        }
        if (!hasAttributeQuery) {
            return XPathType.MULTIPLE_ELEMENTS;
        } else {
            if (!hasSingleQuery) {
                if (!hasSpecifyingQuery) {
                    return XPathType.MULTIPLE_ATTRIBUTES;
                }
                else {
                    return XPathType.MULTIPLE_ELEMENTS;
                }
            } else {
                return XPathType.ELEMENT;
            }
        }
    }

    public static void main(String args[]) {
        System.out.println("args = " + evaluateXPath("/book"));
        System.out.println(evaluateXPath("//router"));
        System.out.println(evaluateXPath("/routerset"));
        System.out.println(evaluateXPath("//router[@id='123.5.1.2.3.4']"));
        System.out.println(evaluateXPath("//ns:router[@id='123']"));
        System.out.println(evaluateXPath("//.:"));
        System.out.println(evaluateXPath("/driver/hello[@x='123123']"));
        System.out.println(evaluateXPath("/driver/*"));
        System.out.println(evaluateXPath("/driver[@*]"));
        System.out.println(evaluateXPath("//@lang"));
        System.out.println(evaluateXPath("/bookstore/book/title | //price"));
        System.out.println(evaluateXPath("/bookstore/book[price>35.00]"));
        System.out.println(evaluateXPath("@*"));
    }

}
