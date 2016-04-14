package net.onenandone.fralax.parser;

import com.ximpleware.*;
import net.onenandone.fralax.FralaxException;
import net.onenandone.fralax.XmlContext;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;

/**
 * Represents a valid XML Document parsed from a file. Can be further navigated using xpath queries.
 *
 * @author Daniel Draper Johann Böhler
 * @version 1.0
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
    VtdXmlParserContext(final String file) throws IOException, ParseException {
        final File fileToParse = new File(file);
        try (final FileInputStream fileInputStream = new FileInputStream(fileToParse)) {
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
    }

    private VtdXmlParserContext(final AutoPilot autopilot, final VTDNav navigation, final Map<String, String> registeredNamespaces) {
        this.autopilot = autopilot;
        this.navigation = navigation;
        this.registeredNamespaces = registeredNamespaces;
    }

    @Override
    /**
     * @see net.onenandone.fralax.XmlContext#registerNamespace(String, String)
     */
    public void registerNamespace(String key, String value) {
        registeredNamespaces.put(key, value);
        addNamespacesToAutopilot(autopilot, registeredNamespaces);
    }


    /**
     * Adds all registered Namespaces to the Autopilot for evaluation.
     */
    private static void addNamespacesToAutopilot(final AutoPilot autopilot, final Map<String, String> registeredNamespaces) {
        for (Map.Entry<String, String> entry : registeredNamespaces.entrySet()) {
            autopilot.declareXPathNameSpace(entry.getKey(), entry.getValue());
        }
    }

    @Override
    /**
     * @see XmlContext#select(String)
     */
    public Optional<XmlContext> select(String xpath) throws FralaxException {
        final List<XmlContext> result = selectAll(xpath);
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
        final List<XmlContext> xmlElements = new ArrayList<>();

        final VTDNav selectionNavigation = navigation.cloneNav();
        final AutoPilot selectionAutoPilot = new AutoPilot(selectionNavigation);
        addNamespacesToAutopilot(selectionAutoPilot, registeredNamespaces);

        try {
            selectionAutoPilot.selectXPath(xpath);

            int xpathResultIndex = selectionAutoPilot.evalXPath();
            while (xpathResultIndex != -1) {
                //Take into account searches for Attribute/Value of an Element
                if (selectionNavigation.getTokenType(xpathResultIndex) == VTDNav.TOKEN_CHARACTER_DATA) {
                    xmlElements.add(new ValueContext(selectionNavigation.toNormalizedString(xpathResultIndex)));
                } else if (selectionNavigation.getTokenType(xpathResultIndex) == VTDNav.TOKEN_ATTR_NAME) {
                    xmlElements.add(new ValueContext(selectionNavigation.toNormalizedString(xpathResultIndex + 1)));
                } else {
                    final VTDNav clonedNavigation = selectionNavigation.cloneNav();
                    final AutoPilot clonedAutoPilot = new AutoPilot(clonedNavigation);
                    xmlElements.add(new VtdXmlParserContext(clonedAutoPilot, clonedNavigation, registeredNamespaces));
                }
                xpathResultIndex = selectionAutoPilot.evalXPath();
            }
            return xmlElements;
        } catch (XPathEvalException | NavException e) {
            if (e.getMessage().contains("binary")) {
                throw new FralaxException("Binary Expressions are not supported", e);
            } else {
                throw new FralaxException("Error when navigating through XPathResults", e);
            }
        } catch (XPathParseException e) {
            throw new FralaxException("Xpath can not be selected from Parser", e);
        }
    }

    @Override
    /**
     * @see XmlContext#asString()
     */
    public String asString() {
        final VTDNav selectionNavigation = navigation.cloneNav();
        try {
            final int index = selectionNavigation.getCurrentIndex();
            String curElement = "<" + selectionNavigation.toNormalizedString(index);
            for (String attribute : evaluateAttributes()) {
                curElement = curElement + " " + attribute;
            }
            curElement = curElement + ">";
            String curOldElement = curElement;
            final ChildrenAndSiblings childrenAndSiblings = evaluateChildrenAndSiblings(selectionNavigation.getCurrentDepth(), index, selectionNavigation.getCurrentDepth());
            for (final String childChild : childrenAndSiblings.children) {
                curElement = curElement + childChild;
            }
            for (final String childSibling : childrenAndSiblings.siblings) {
                curElement = curElement + childSibling;
            }
            if (curOldElement.equals(curElement)) {
                selectionNavigation.recoverNode(index);
                curElement = curElement + selectionNavigation.getXPathStringVal();
            }
            curElement = curElement + "</" + selectionNavigation.toNormalizedString(index) + ">";

            return curElement;
        } catch (NavException e) {
            throw new FralaxException("failed to transform to string", e);
        }
    }

    @Override
    /**
     * @see XmlContext#asFormattedString()
     */
    public String asFormattedString() {
        String toBeFormatted;
        try {
            toBeFormatted = asStringWithoutNamespaces();
            final Source xmlInput = new StreamSource(new StringReader(toBeFormatted));
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
     * Returns the XML Element(s) without any namespace prefixes, e.g. instead of <x:books></x:books> this will return <books></books>.
     * @return the XML string representation unformatted without name space prefixes.
     */
    private String asStringWithoutNamespaces() {
        final VTDNav selectionNavigation = navigation.cloneNav();
        try {
            final int index = selectionNavigation.getCurrentIndex();
            String curElement = "<" + selectionNavigation.toNormalizedString(index).replaceFirst("(.*):", "");
            for (String attribute : evaluateAttributes(false)) {
                curElement = curElement + " " + attribute;
            }
            curElement = curElement + ">";
            String curOldElement = curElement;
            final ChildrenAndSiblings childrenAndSiblings = evaluateChildrenAndSiblings(selectionNavigation.getCurrentDepth(), index, selectionNavigation.getCurrentDepth(), false);
            for (final String childChild : childrenAndSiblings.children) {
                curElement = curElement + childChild;
            }
            for (final String childSibling : childrenAndSiblings.siblings) {
                curElement = curElement + childSibling;
            }
            if (curOldElement.equals(curElement)) {
                selectionNavigation.recoverNode(index);
                curElement = curElement + selectionNavigation.getXPathStringVal().replaceFirst("(.*):", "");
            }
            curElement = curElement + "</" + selectionNavigation.toNormalizedString(index).replaceFirst("(.*):", "") + ">";

            return curElement;
        } catch (NavException e) {
            throw new FralaxException("failed to transform to string", e);
        }
    }



    private List<String> evaluateAttributes() throws NavException {
        return evaluateAttributes(true);
    }


    /**
     * Used to get all Attributes of the Current Node.
     *
     * @param withNamespaces to specify if namespaces are cut from attribute names (when false) or not (when true).
     * @return A List of all XMLAttributes in the current Node.
     * @throws NavException When an Error occurs navigating the attributes;
     */
    private List<String> evaluateAttributes(final boolean withNamespaces) throws NavException {
        final List<String> attributes = new ArrayList<>();
        final int attrCount = navigation.getAttrCount();
        final int curIndex = navigation.getCurrentIndex();
        for (int i = curIndex + 1; i < curIndex + 1 + attrCount * 2; i += 2) {
            String attributeKey;
            if (withNamespaces) {
                attributeKey = navigation.toNormalizedString(i);
            }
            else {
                attributeKey = navigation.toNormalizedString(i).replaceFirst("(.*):", "");
            }
            final String attributeValue = navigation.toRawString(i + 1);
            attributes.add(attributeKey + "=\"" + attributeValue + "\"");
        }
        return attributes;
    }


    /**
     * @see this#evaluateChildrenAndSiblings(int, int, int, boolean)
     */
    private ChildrenAndSiblings evaluateChildrenAndSiblings(final int rootDepth, final int parentIndex, final int startDepth) throws NavException {
        return evaluateChildrenAndSiblings(rootDepth, parentIndex, startDepth, true);
    }


    /**
     * Used to get All Children of the Current Node as Well as its Siblings. Uses Recursive DFS to find all nodes required then parses them into XMLElements
     * and returns as List of Lists with the first element being the children and the second Element being the siblings (each as its own XMLElement List).
     *
     * @param rootDepth   the rootDepth index we start the search from.
     * @param parentIndex the parent Index of the current search.
     * @param startDepth  the startDepth of the current iteration.
     * @param withNamespaces to specify if namespaces are cut from all names (when false) or not (when true).
     * @return A List of Lists with 2 elements: Element at index 0 is the children of the current Node. Element at index 1 is the siblings of the current node.
     * @throws NavException Error that occurs when the traversing of Nodes fails.
     */
    private ChildrenAndSiblings evaluateChildrenAndSiblings(final int rootDepth, final int parentIndex, final int startDepth, boolean withNamespaces) throws NavException {
        ChildrenAndSiblings childrenAndSiblings = new ChildrenAndSiblings();
        //Traversing children, uses startDepth as a check as we don't need to traverse already visited child nodes.
        if (navigation.toElement(VTDNav.FIRST_CHILD) && startDepth < navigation.getCurrentDepth()) {
            if (withNamespaces) {
                traverse(rootDepth, startDepth, childrenAndSiblings.children, true);
            }
            else {
                traverse(rootDepth, startDepth, childrenAndSiblings.children, false);
            }
        } else {
            if (withNamespaces) {
                childrenAndSiblings.children.add(navigation.getXPathStringVal());
            }
            else {
                childrenAndSiblings.children.add(navigation.getXPathStringVal().replaceFirst("(.*):", ""));
            }
        }

        //After traversing all children nodes we now go back to our parent element and traverse all our siblings.
        navigation.recoverNode(parentIndex);
        //Assignment so the next sibling traversal uses correct depth to determine if we should search for more children.
        final int newStartDepth = startDepth - 1;
        //Traversing siblings, uses rootDepth in the check so we don't keep on checking siblings of the node we start our search from.
        if (navigation.toElement(VTDNav.NEXT_SIBLING) && rootDepth < navigation.getCurrentDepth()) {
            if (withNamespaces) {
                traverse(rootDepth, newStartDepth, childrenAndSiblings.siblings, true);
            }
            else {
                traverse(rootDepth, newStartDepth, childrenAndSiblings.siblings, false);
            }

        }
        return childrenAndSiblings;
    }


    /**
     * @see this#traverse(int, int, List, boolean)
     */
    private void traverse(final int rootDepth, final int startDepth, final List<String> elements) throws NavException {
        traverse(rootDepth, startDepth, elements, true);
    }

    private void traverse(final int rootDepth, final int startDepth, final List<String> elements, final boolean withNamespaces) throws NavException {
        int curIndex = navigation.getCurrentIndex();
        String child = "<";
        if (withNamespaces) {
            child = child + navigation.toNormalizedString(curIndex);
        }
        else {
            child = child + navigation.toNormalizedString(curIndex).replaceFirst("(.*):", "");
        }
        if (withNamespaces) {
            for (final String attribute : evaluateAttributes()) {
                child = child + " " + attribute;
            }
        }
        else {
            for (final String attribute : evaluateAttributes(false)) {
                child = child + " " + attribute;
            }
        }

        child = child + ">";
        ChildrenAndSiblings childrenAndSiblings;
        if (withNamespaces) {
            childrenAndSiblings = evaluateChildrenAndSiblings(rootDepth, curIndex, startDepth + 1);
        }
        else {
            childrenAndSiblings = evaluateChildrenAndSiblings(rootDepth, curIndex, startDepth + 1, false);
        }
        for (final String childChild : childrenAndSiblings.children) {
            child = child + childChild;
        }
        if (withNamespaces) {
            child = child + "</" + navigation.toNormalizedString(curIndex) + ">";
        }
        else {
            child = child + "</" + navigation.toNormalizedString(curIndex).replaceFirst("(.*):", "") + ">";
        }
        elements.add(child);
        elements.addAll(childrenAndSiblings.siblings);
    }


    private static class ChildrenAndSiblings {
        private final List<String> children = new ArrayList<>();
        private final List<String> siblings = new ArrayList<>();
    }

}
