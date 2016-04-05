package net.onenandone.fralax.example;

import com.ximpleware.*;
import lombok.extern.java.Log;
import net.onenandone.fralax.model.ListOfXMLAttributes;
import net.onenandone.fralax.model.ListOfXMLElements;
import net.onenandone.fralax.model.XMLAttribute;
import net.onenandone.fralax.model.XMLElement;
import net.onenandone.fralax.parser.XMLParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 */
@Log
public class VtdXMLParserImpl extends XMLParser {

    private AutoPilot autopilot;
    private VTDNav navigation;


    /**
     * Creates the VtdXMlParser in the FraLaX Framework.
     * @param fileToParse File to parse.
     * @param namespaces Map of Namespaces in use for XpathRequests.
     * @throws IOException Thrown when error occurs on opening the XML File.
     * @throws ParseException Thrown when error occurs on first parsing the XML File.
     */
    public VtdXMLParserImpl(File fileToParse, Map<String, String> namespaces) throws IOException, ParseException {
        super(fileToParse, namespaces);
        FileInputStream fileInputStream;
        fileInputStream = new FileInputStream(fileToParse);
        byte[] xmlByteArray = new byte[(int) fileToParse.length()];
        if (fileInputStream.read(xmlByteArray) != xmlByteArray.length) {
            throw new IOException("Error when reading the XML File");
        }
        final VTDGen vtdGen = new VTDGen();
        vtdGen.setDoc(xmlByteArray);
        vtdGen.parse(true); // set namespace awareness to true
        navigation = vtdGen.getNav();
        autopilot = new AutoPilot(navigation);
        for (Map.Entry<String, String> namespace : namespaces.entrySet()) {
            autopilot.declareXPathNameSpace(namespace.getKey(), namespace.getValue());
        }
    }

    @Override
    /**
     * @see XMLParser#searchForAllAttributes(String)
     */
    protected ListOfXMLAttributes searchForAllAttributes(String xPathQuery) {
        ListOfXMLAttributes result = new ListOfXMLAttributes(new ArrayList<>());
        try {
            autopilot.selectXPath(xPathQuery);
        } catch (XPathParseException e) {
            log.log(Level.SEVERE, "XPath parsing failed in Parser Implementation, should never happen!");
            //Exception would have been handled in XMLParser
        }
        try {
            int currentXPathIndex = autopilot.evalXPath();
            while (currentXPathIndex != -1) {
                XMLAttribute curAttribute = new XMLAttribute(navigation.toNormalizedString(navigation.getCurrentIndex()), navigation.toNormalizedString(navigation.getCurrentIndex() + 1));
                result.getAttributeList().add(curAttribute);
                currentXPathIndex = autopilot.evalXPath();
            }
        } catch (XPathEvalException | NavException e) {
            if (e.getMessage().contains("binary")) {
                log.log(Level.WARNING, "Binary Expressions are not supported" + e.getMessage());
            }
            log.log(Level.WARNING, e.getMessage());
            return null;
        }

        return result;
    }

    @Override
    /**
     * @see XMLParser#searchForAttribute(String)
     */
    protected XMLAttribute searchForAttribute(String xPathQuery) {
        ListOfXMLAttributes result = searchForAllAttributes(xPathQuery);
        if (result.getAttributeList().size() > 0) {
            return result.getAttributeList().get(0);
        }
        else {
            return null;
        }
    }

    @Override
    /**
     * @see XMLParser#searchForElement(String)
     */
    protected XMLElement searchForElement(String xPathQuery) {
        ListOfXMLElements result = searchForAllElements(xPathQuery);
        if (result.getElementList().size() > 0) {
            return result.getElementList().get(0);
        } else {
            return null;
        }
    }


    @Override
    /**
     * @see XMLParser#searchForAllElements(String)
     */
    protected ListOfXMLElements searchForAllElements(String xPathQuery) {
        try {
            autopilot.selectXPath(xPathQuery);
        } catch (XPathParseException e) {
            log.log(Level.SEVERE, "XPath parsing failed in Parser Implementation, should never happen!", e);
            //Exception would have been handled in XMLParser
        }
        try {
            int xpathResultIndex = autopilot.evalXPath();
            int curDepth = navigation.getCurrentDepth();
            ListOfXMLElements result = new ListOfXMLElements(new ArrayList<>());
            while (xpathResultIndex != -1) {
                XMLElement curElement = new XMLElement(navigation.toNormalizedString(xpathResultIndex));
                List<XMLAttribute> attributes = evaluateAttributes();
                curElement.getAttributes().addAll(attributes);
                List<List<XMLElement>> children = evaluateChildrenAndSiblings(curDepth, xpathResultIndex, curDepth);
                //Add the children as well as its siblings.
                curElement.getChildren().addAll(children.get(0));
                curElement.getChildren().addAll(children.get(1));
                result.getElementList().add(curElement);
                xpathResultIndex = autopilot.evalXPath();
            }
            return result;
        } catch (XPathEvalException | NavException e) {
            if (e.getMessage().contains("binary")) {
                log.log(Level.WARNING, "Binary Expressions are not supported", e);
            }
            else {
                log.log(Level.WARNING, "Error when navigating through XPathResults", e);
            }

            return null;
        }
    }

    /**
     * Used to get all Attributes of the Current Node.
     * @return A List of all XMLAttributes in the current Node.
     * @throws NavException When an Error occurs navigating the attributes;
     */
    private List<XMLAttribute> evaluateAttributes() throws NavException {
        final List<XMLAttribute> attributes = new ArrayList<>();
        int attrCount = navigation.getAttrCount();
        for (int i = navigation.getCurrentIndex() + 1; i <= navigation.getCurrentIndex() + 1 + attrCount; i += 2) {
            String attributeKey = navigation.toNormalizedString(i);
            String attributeValue = navigation.toNormalizedString(i + 1);
            attributes.add(new XMLAttribute(attributeKey, attributeValue));
        }
        return attributes;
    }


    /**
     * Used to get All Children of the Current Node as Well as its Siblings. Uses Recursive DFS to find all nodes required then parses them into XMLElements
     * and returns as List of Lists with the first element being the children and the second Element being the siblings (each as its own XMLElement List).
     * @param rootIndex the rootIndex index we start the search from.
     * @param parentIndex the parent Index of the current search.
     * @param startDepth the startDepth of the current iteration.
     * @return A List of Lists with 2 elements: Element at index 0 is the children of the current Node. Element at index 1 is the siblings of the current node.
     * @throws NavException Error that occurs when the traversing of Nodes fails.
     */
    private List<List<XMLElement>> evaluateChildrenAndSiblings(int rootIndex, int parentIndex, int startDepth) throws NavException {
        List<XMLElement> children = new ArrayList<>();
        List<XMLElement> siblings = new ArrayList<>();
        List<List<XMLElement>> result = new ArrayList<>();
        result.add(children);
        result.add(siblings);
        //Traversing children, uses startDepth as a check as we don't need to traverse already visited child nodes.
        if (navigation.toElement(VTDNav.FIRST_CHILD) && startDepth < navigation.getCurrentDepth()) {
            int curIndex = navigation.getCurrentIndex();
            XMLElement child = new XMLElement(navigation.toNormalizedString(curIndex));
            child.getAttributes().addAll(evaluateAttributes());
            List<List<XMLElement>> newChildren = evaluateChildrenAndSiblings(rootIndex, curIndex, startDepth + 1);
            child.getChildren().addAll(newChildren.get(0));
            children.add(child);
            children.addAll(newChildren.get(1));
        }
        //After traversing all children nodes we now go back to our parent element and traverse all our siblings.
        navigation.recoverNode(parentIndex);
        //Assignment so the next sibling traversal uses correct depth to determine if we should search for more children.
        startDepth--;
        //Traversing siblings, uses rootIndex in the check so we don't keep on checking siblings of the node we start our search from.
        if (navigation.toElement(VTDNav.NEXT_SIBLING) && rootIndex < navigation.getCurrentDepth()) {
            int curIndex = navigation.getCurrentIndex();
            XMLElement sibling = new XMLElement(navigation.toNormalizedString(curIndex));
            sibling.getAttributes().addAll(evaluateAttributes());
            List<List<XMLElement>> newChildren = evaluateChildrenAndSiblings(rootIndex, curIndex, startDepth + 1);
            sibling.getChildren().addAll(newChildren.get(0));
            siblings.add(sibling);
            siblings.addAll(newChildren.get(1));
        }
        return result;
    }


}
