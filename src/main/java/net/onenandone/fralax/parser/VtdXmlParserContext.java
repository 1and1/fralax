package net.onenandone.fralax.parser;

import com.ximpleware.*;
import net.onenandone.fralax.XmlContext;
import net.onenandone.fralax.FralaxException;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;

/**
 * @author Daniel Draper Johann Bähler
 *         Created on 06.04.16.
 * @version 1.0
 *          Represents a valid XML Document parsed from a file. Can be further navigated using xpath queries.
 */
class VtdXmlParserContext implements XmlContext {

    private AutoPilot autopilot;
    private VTDNav navigation;
    private Map<String, String> registeredNamespaces = new HashMap<>();


    /**
     * Default constructor used to create a newly parsed XMLContext from a certain file.
     *
     * @param file the file to parse.
     * @throws IOException    thrown when an error occurs while opening the file.
     * @throws ParseException thrown when an error occurs during parsing of the xml file.
     */
    VtdXmlParserContext(String file) throws IOException, ParseException {
        File fileToParse = new File(file);
        FileInputStream fileInputStream = new FileInputStream(fileToParse);
        byte[] xmlByteArray = new byte[(int) fileToParse.length()];
        if (fileInputStream.read(xmlByteArray) != xmlByteArray.length) {
            throw new IOException("Error when reading the XML File");
        }
        final VTDGen vtdGen = new VTDGen();
        vtdGen.setDoc(xmlByteArray);
        vtdGen.parse(true); // set namespace awareness to true
        navigation = vtdGen.getNav();
        autopilot = new AutoPilot(navigation);
    }

    /**
     * Private Constructor used to create new contexts from the result of an earlier XPathQuery by passing it as its byte value and reapplying the VTD-XML-Parser.
     *
     * @param xmlObjectAsByteArray XmlObject to parse as a byte array.
     * @param registeredNamespaces Namespaces to use for the creation, as we have to set the same namespaces as were used in the earlier XPathSearch.
     * @throws ParseException when an error occurs parsing the input object.
     */
    private VtdXmlParserContext(byte[] xmlObjectAsByteArray, Map<String, String> registeredNamespaces) throws ParseException {
        this.registeredNamespaces = registeredNamespaces;
        addNamespacesToAutopilot();
        final VTDGen vtdGen = new VTDGen();
        vtdGen.setDoc(xmlObjectAsByteArray);
        vtdGen.parse(true); // set namespace awareness to true
        navigation = vtdGen.getNav();
        autopilot = new AutoPilot(navigation);
    }


    @Override
    /**
     * @see net.onenandone.fralax.XmlContext#registerNamespace(String, String)
     */
    public void registerNamespace(String key, String value) {
        registeredNamespaces.put(key, value);
        addNamespacesToAutopilot();
    }


    /**
     * Adds all registered Namespaces to the Autopilot for evaluation.
     */
    private void addNamespacesToAutopilot() {
        for (Map.Entry<String, String> entry : registeredNamespaces.entrySet()) {
            autopilot.declareXPathNameSpace(entry.getKey(), entry.getValue());
        }
    }

    @Override
    /**
     * @see XmlContext#select(String)
     */
    public Optional<XmlContext> select(String xpath) throws FralaxException {
        try {
            autopilot.selectXPath(xpath);
        } catch (XPathParseException e) {
            throw new FralaxException("Xpath can not be selected from Parser", e);
        }
        List<XmlContext> result = selectAll(xpath);
        if (result.size() > 1) {
            throw new FralaxException("Tried to select one Element as result, but result was " + result.size() + " elements large.");
        } else if (result.size() == 1) {
            return Optional.of(result.get(0));
        } else {
            return Optional.empty();
        }
    }

    @Override
    /**
     * @see XmlContext#selectAll(String)
     */
    public List<XmlContext> selectAll(String xpath) throws FralaxException {
        List<XmlContext> xmlElements = new ArrayList<>();
        try {
            autopilot.selectXPath(xpath);
        } catch (XPathParseException e) {
            throw new FralaxException("Xpath can not be selected from Parser", e);
        }
        try {
            List<String> selectionAsStrings = new ArrayList<>();
            int xpathResultIndex = autopilot.evalXPath();
            while (xpathResultIndex != -1) {
                //Take into account searches for Attribute/Value of an Element
                if (navigation.getTokenType(xpathResultIndex) == VTDNav.TOKEN_CHARACTER_DATA) {
                    xmlElements.add(new ValueContext(navigation.toNormalizedString(xpathResultIndex)));
                } else if (navigation.getTokenType(xpathResultIndex) == VTDNav.TOKEN_ATTR_NAME) {
                    xmlElements.add(new ValueContext(navigation.toNormalizedString(xpathResultIndex + 1)));
                } else {
                    String curElement = "<" + navigation.toNormalizedString(xpathResultIndex);
                    for (String attribute : evaluateAttributes()) {
                        curElement = curElement + " " + attribute;
                    }
                    curElement = curElement + ">";
                    String curOldElement = curElement;
                    List<List<String>> childrenAndSiblings = evaluateChildrenAndSiblings(navigation.getCurrentDepth(), xpathResultIndex, navigation.getCurrentDepth());
                    for (String childChild : childrenAndSiblings.get(0)) {
                        curElement = curElement + childChild;
                    }
                    for (String childSibling : childrenAndSiblings.get(1)) {
                        curElement = curElement + childSibling;
                    }
                    if (curOldElement.equals(curElement)) {
                        navigation.recoverNode(xpathResultIndex);
                        curElement = curElement + navigation.getXPathStringVal();
                    }
                    curElement = curElement + "</" + navigation.toNormalizedString(xpathResultIndex) + ">";
                    selectionAsStrings.add(curElement);
                    navigation.recoverNode(xpathResultIndex);
                }
                xpathResultIndex = autopilot.evalXPath();
            }
            for (String s : selectionAsStrings) {
                xmlElements.add(new VtdXmlParserContext(s.getBytes(), registeredNamespaces));
            }
            return xmlElements;
        } catch (XPathEvalException | NavException e) {
            if (e.getMessage().contains("binary")) {
                throw new FralaxException("Binary Expressions are not supported", e);
            } else {
                throw new FralaxException("Error when navigating through XPathResults", e);
            }
        } catch (ParseException e) {
            throw new FralaxException("Error when parsing result of XPathSearch", e);
        }
    }

    @Override
    /**
     * @see XmlContext#asString()
     */
    public String asString() {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try {
            navigation.dumpXML(byteOutputStream);
        } catch (IOException e) {
            throw new FralaxException("failed to dump xml state", e);
        }
        return byteOutputStream.toString();
    }

    @Override
    /**
     * @see XmlContext#asFormattedString()
     */
    public String asFormattedString() {
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

    /**
     * Used to get all Attributes of the Current Node.
     *
     * @return A List of all XMLAttributes in the current Node.
     * @throws NavException When an Error occurs navigating the attributes;
     */
    private List<String> evaluateAttributes() throws NavException {
        final List<String> attributes = new ArrayList<>();
        int attrCount = navigation.getAttrCount();
        int curIndex = navigation.getCurrentIndex();
        for (int i = curIndex + 1; i < curIndex + 1 + attrCount * 2; i += 2) {
            String attributeKey = navigation.toRawString(i);
            String attributeValue = navigation.toRawString(i + 1);
            attributes.add(attributeKey + "=\"" + attributeValue + "\"");
        }
        return attributes;
    }


    /**
     * Used to get All Children of the Current Node as Well as its Siblings. Uses Recursive DFS to find all nodes required then parses them into XMLElements
     * and returns as List of Lists with the first element being the children and the second Element being the siblings (each as its own XMLElement List).
     *
     * @param rootDepth   the rootDepth index we start the search from.
     * @param parentIndex the parent Index of the current search.
     * @param startDepth  the startDepth of the current iteration.
     * @return A List of Lists with 2 elements: Element at index 0 is the children of the current Node. Element at index 1 is the siblings of the current node.
     * @throws NavException Error that occurs when the traversing of Nodes fails.
     */
    private List<List<String>> evaluateChildrenAndSiblings(int rootDepth, int parentIndex, int startDepth) throws NavException {
        List<String> children = new ArrayList<>();
        List<String> siblings = new ArrayList<>();
        List<List<String>> result = new ArrayList<>();
        result.add(children);
        result.add(siblings);
        //Traversing children, uses startDepth as a check as we don't need to traverse already visited child nodes.
        if (navigation.toElement(VTDNav.FIRST_CHILD) && startDepth < navigation.getCurrentDepth()) {
            int curIndex = navigation.getCurrentIndex();
            String child = "<";
            child = child + navigation.toNormalizedString(curIndex);
            for (String attribute : evaluateAttributes()) {
                child = child + " " + attribute;
            }
            child = child + ">";
            List<List<String>> newChildren = evaluateChildrenAndSiblings(rootDepth, curIndex, startDepth + 1);
            for (String childChild : newChildren.get(0)) {
                child = child + childChild;
            }
            child = child + "</" + navigation.toNormalizedString(curIndex) + ">";
            children.add(child);
            children.addAll(newChildren.get(1));
        } else {
            children.add(navigation.getXPathStringVal());
        }

        //After traversing all children nodes we now go back to our parent element and traverse all our siblings.
        navigation.recoverNode(parentIndex);
        //Assignment so the next sibling traversal uses correct depth to determine if we should search for more children.
        startDepth--;
        //Traversing siblings, uses rootDepth in the check so we don't keep on checking siblings of the node we start our search from.
        if (navigation.toElement(VTDNav.NEXT_SIBLING) && rootDepth < navigation.getCurrentDepth()) {
            int curIndex = navigation.getCurrentIndex();
            String sibling = "<";
            sibling = sibling + navigation.toNormalizedString(curIndex);
            for (String attribute : evaluateAttributes()) {
                sibling = sibling + " " + attribute;
            }
            sibling = sibling + ">";
            List<List<String>> newChildren = evaluateChildrenAndSiblings(rootDepth, curIndex, startDepth + 1);
            for (String childChild : newChildren.get(0)) {
                sibling = sibling + childChild;
            }
            sibling = sibling + "</" + navigation.toNormalizedString(curIndex) + ">";
            siblings.add(sibling);
            siblings.addAll(newChildren.get(1));
        }
        return result;
    }

}
