package net.onenandone.fralax.parser;

import com.ximpleware.*;
import net.onenandone.fralax.FralaxException;
import net.onenandone.fralax.XmlContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a valid XML Document parsed from a file. Can be further navigated using xpath queries.
 */
class VtdXmlParserContext implements XmlContext {

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("xmlns((:)([a-z]))?");

    private AutoPilot autopilot;
    private VTDNav navigation;
    private Map<String, String> registeredNamespaces = new HashMap<>();
    private String xpath = "";

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

            analyzeNamespaces();
        }
    }

    /**
     * Analyzes namespaces of the specified XML file.
     * <p />
     * All namespaces will be registered by the prefix defined in the XML. The Xpath will therefore rely on the same namespace prefixes as of the XML.
     */
    private void analyzeNamespaces() {
        final VTDNav namespaceNavigation = navigation.cloneNav();
        boolean rootElementAnalyzed = false;
        int index = namespaceNavigation.getRootIndex();
        while (!rootElementAnalyzed) {
            index++;
            if (namespaceNavigation.getTokenType(index) == VTDNav.TOKEN_STARTING_TAG || namespaceNavigation.getTokenType(index) == VTDNav.TOKEN_ENDING_TAG) {
                rootElementAnalyzed = true;
            } else {
                try {
                    if (namespaceNavigation.getTokenType(index) == VTDNav.TOKEN_ATTR_NS) {
                        final Matcher namespaceMatcher = NAMESPACE_PATTERN.matcher(namespaceNavigation.toString(index));
                        if (namespaceMatcher.matches()) {
                            final String prefix = namespaceMatcher.group(3);
                            index++;
                            if (prefix != null) {
                                registeredNamespaces.put(prefix, namespaceNavigation.toString(index));
                                addNamespacesToAutopilot(autopilot, registeredNamespaces);
                            }
                        }
                    }
                } catch (final NavException e) {
                    throw new FralaxException("could not parse namespaces", e);
                }
            }
        }
    }

    /**
     * Constructor used to create a newly parsed XMLContext from an XPath Result. takes a freshly created Autopilot
     * and navigation in which the result is embedded.
     *
     * @param autopilot            autopilot for the specified navigation.
     * @param navigation           navigation to navigate through the xpath result.
     * @param registeredNamespaces namespaces to register for the new xml context.
     */
    private VtdXmlParserContext(final String xpath, final AutoPilot autopilot, final VTDNav navigation, final Map<String, String> registeredNamespaces) {
        this.autopilot = autopilot;
        this.navigation = navigation;
        this.registeredNamespaces = registeredNamespaces;
        this.xpath = xpath;
    }

    /** Adds all registered Namespaces to the Autopilot for evaluation. */
    private static void addNamespacesToAutopilot(final AutoPilot autopilot, final Map<String, String> registeredNamespaces) {
        for (Map.Entry<String, String> entry : registeredNamespaces.entrySet()) {
            autopilot.declareXPathNameSpace(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Optional<XmlContext> select(final String xpath) throws FralaxException {
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
    public List<XmlContext> selectAll(String xpath) throws FralaxException {
        final List<XmlContext> xmlElements = new ArrayList<>();

        final VTDNav selectionNavigation = navigation.cloneNav();
        final AutoPilot selectionAutoPilot = new AutoPilot(selectionNavigation);
        addNamespacesToAutopilot(selectionAutoPilot, registeredNamespaces);

        try {
            if (!this.xpath.equals("") && (xpath.startsWith("/") || xpath.startsWith("//"))) {
                xpath = this.xpath + xpath;
            }
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
                    xmlElements.add(new VtdXmlParserContext(xpath, clonedAutoPilot, clonedNavigation, registeredNamespaces));
                }
                xpathResultIndex = selectionAutoPilot.evalXPath();
            }
            return xmlElements;
        } catch (XPathEvalException | NavException e) {
            if (" Function Expr can't eval to node set ".equals(e.getMessage())) {
                return Collections.singletonList(new ValueContext(selectionAutoPilot.evalXPathToString()));
            } else if (e.getMessage().contains("binary")) {
                throw new FralaxException("Binary Expressions are not supported", e);
            } else {
                throw new FralaxException("Error when navigating through XPathResults", e);
            }
        } catch (XPathParseException e) {
            throw new FralaxException("Xpath can not be selected from Parser", e);
        }
    }

    @Override
    public String asString() {
        return asString(false);
    }

    @Override
    public String asString(final boolean formatted) {
        final VTDNav selectionNavigation = navigation.cloneNav();
        if (selectionNavigation.getCurrentIndex() == selectionNavigation.getRootIndex()) {
            try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                selectionNavigation.dumpXML(outputStream);
                return outputStream.toString();
            } catch (final IOException e) {
                // try to create string otherwise
            }
        }
        try {
            final int index = selectionNavigation.getCurrentIndex();
            final StringBuilder curElement = new StringBuilder("<").append(selectionNavigation.toNormalizedString(index));
            for (String attribute : evaluateAttributes()) {
                curElement.append(" ").append(attribute);
            }
            final ChildrenAndSiblings childrenAndSiblings = evaluateChildrenAndSiblings(formatted, selectionNavigation.getCurrentDepth(), index, selectionNavigation.getCurrentDepth());
            //check size so we can be sure this isn't just a single value object/an empty object (e.g <author>Hitchcock</author> shouldn't be linebroken/indented.)
            curElement.append(formatted && childrenAndSiblings.children.size() > 1 ? ">\n" : ">");
            String oldCurElement = curElement.toString();
            //same as above comment
            childrenAndSiblings.children.forEach(formatted  && childrenAndSiblings.children.size() > 1 ? curElement.append("    ")::append : curElement::append);
            childrenAndSiblings.siblings.forEach(curElement::append);
            if (curElement.toString().equals(oldCurElement)) {
                selectionNavigation.recoverNode(index);
                curElement.append(selectionNavigation.getXPathStringVal());
            }
            curElement.append("</").append(selectionNavigation.toNormalizedString(index)).append(">");
            return curElement.toString();
        } catch (NavException e) {
            throw new FralaxException("failed to transform to string", e);
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
        final int attrCount = navigation.getAttrCount();
        final int curIndex = navigation.getCurrentIndex();
        for (int i = curIndex + 1; i < curIndex + 1 + attrCount * 2; i += 2) {
            final String attributeKey = navigation.toNormalizedString(i);
            final String attributeValue = navigation.toRawString(i + 1);
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
    private ChildrenAndSiblings evaluateChildrenAndSiblings(final boolean formatted, final int rootDepth, final int parentIndex, final int startDepth) throws NavException {
        ChildrenAndSiblings childrenAndSiblings = new ChildrenAndSiblings();
        final int startNesting = navigation.getNestingLevel();
        //Traversing children, uses startDepth as a check as we don't need to traverse already visited child nodes.
        if (navigation.toElement(VTDNav.FIRST_CHILD) && startDepth < navigation.getCurrentDepth()) {
            traverse(formatted, startNesting, rootDepth, startDepth, childrenAndSiblings.children);
        } else {
            childrenAndSiblings.children.add(navigation.getXPathStringVal());
        }

        //After traversing all children nodes we now go back to our parent element and traverse all our siblings.
        navigation.recoverNode(parentIndex);
        //Traversing siblings, uses rootDepth in the check so we don't keep on checking siblings of the node we start our search from.
        if (navigation.toElement(VTDNav.NEXT_SIBLING) && rootDepth < navigation.getCurrentDepth()) {
            //depth /level -1 to account for traversing back to parent
            traverse(formatted, startNesting - 1, rootDepth, startDepth - 1, childrenAndSiblings.siblings);
        }
        return childrenAndSiblings;
    }

    /**
     * Traverses all child and its sibling elements for a certain element. Uses a DFS approach: go to deepest
     * element as long as possible, then resolve the children.
     *
     * @param rootDepth  the depth of the root element of this context.
     * @param startDepth the depth we started our search on (so the depth of the first result for the xpath).
     * @param elements   the elements to fill.
     * @throws NavException thrown when an error occurs navigating through the context.
     */
    private void traverse(final boolean formatted, final int startNesting, final int rootDepth, final int startDepth, final List<String> elements) throws NavException {
        int curIndex = navigation.getCurrentIndex();
        StringBuilder child = new StringBuilder();
        if (formatted) {
            for (int i = 0; i < navigation.getNestingLevel() - startNesting; i++) {
                child.append("    ");
            }
        }
        child.append("<");
        child.append(navigation.toNormalizedString(curIndex));
        for (final String attribute : evaluateAttributes()) {
            child.append(" ").append(attribute);
        }
        child.append(">");
        ChildrenAndSiblings childrenAndSiblings;
        childrenAndSiblings = evaluateChildrenAndSiblings(formatted, rootDepth, curIndex, startDepth + 1);
        childrenAndSiblings.children.forEach(child::append);
        child.append("</").append(navigation.toNormalizedString(curIndex)).append(">");
        if (formatted) {
            child.append("\n");
        }
        elements.add(child.toString());
        elements.addAll(childrenAndSiblings.siblings);
    }

    /**
     * Class encapsulating two arrayLists, one for children of an xml element one for siblings of the element at the same depth.
     */
    private static class ChildrenAndSiblings {
        private final List<String> children = new ArrayList<>();
        private final List<String> siblings = new ArrayList<>();
    }

}
