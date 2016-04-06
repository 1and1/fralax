package net.onenandone.fralax.example;

import com.ximpleware.*;
import lombok.extern.slf4j.Slf4j;
import net.onenandone.fralax.model.Context;
import net.onenandone.fralax.model.WrongXPathForTypeException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 06.04.16.
 * @version 1.0
 */
@Slf4j
public class VtdParserContext implements Context {

    private AutoPilot autopilot;
    private VTDNav navigation;
    private Map<String, String> registeredNamespaces = new HashMap<>();

    public VtdParserContext(String file) throws IOException, ParseException {
        File fileToParse = new File(file);
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
    }


    private VtdParserContext(byte[] xmlObjectAsByteArray, Map<String, String> registeredNamespaces) throws ParseException {
        this.registeredNamespaces = registeredNamespaces;
        addNamespacesToAutopilot();
        final VTDGen vtdGen = new VTDGen();
        vtdGen.setDoc(xmlObjectAsByteArray);
        vtdGen.parse(true); // set namespace awareness to true
        navigation = vtdGen.getNav();
        autopilot = new AutoPilot(navigation);
    }


    @Override
    public void registerNamespace(String key, String value) {
        registeredNamespaces.put(key, value);
        addNamespacesToAutopilot();
    }

    private void addNamespacesToAutopilot() {
        for (Map.Entry<String, String> entry : registeredNamespaces.entrySet()) {
            autopilot.declareXPathNameSpace(entry.getKey(), entry.getValue());
        }
    }

    @Override
    /**
     * @see Context#select(String)
     */
    public Optional<Context> select(String xpath) throws WrongXPathForTypeException {
        try {
            autopilot.selectXPath(xpath);
        } catch (XPathParseException e) {
            log.error("Xpath can not be selected from Parser", e);
            throw new WrongXPathForTypeException("Xpath can not be selected from Parser");
        }
        List<Context> result = selectAll(xpath);
        if (result.size() > 1) {
            log.error("Xpath selected for ");
            throw new WrongXPathForTypeException("Tried to select one Element as result, but result was " + result.size() + " elements large.");
        } else {
            return Optional.of(result.get(0));
        }
    }

    @Override
    /**
     * @see Context#selectAll(String)
     */
    public List<Context> selectAll(String xpath) throws WrongXPathForTypeException {
        List<Context> xmlElements = new ArrayList<>();
        try {
            autopilot.selectXPath(xpath);
        } catch (XPathParseException e) {
            log.error("Xpath can not be selected from Parser", e);
            throw new WrongXPathForTypeException("Xpath can not be selected from Parser");
        }
        try {
            List<String> selectionAsStrings = new ArrayList<>();
            int xpathResultIndex = autopilot.evalXPath();
            while (xpathResultIndex != -1) {
                String curElement = navigation.getXPathStringVal();
                selectionAsStrings.add(curElement);
                xpathResultIndex = autopilot.evalXPath();
            }
            for (String s : selectionAsStrings) {
                xmlElements.add(new VtdParserContext(s.getBytes(), registeredNamespaces));
            }
            return xmlElements;
        } catch (XPathEvalException | NavException e) {
            if (e.getMessage().contains("binary")) {
                log.warn("Binary Expressions are not supported", e);
                throw new WrongXPathForTypeException("EBinary Expressions are not supported");
            } else {
                log.error("Error when navigating through XPathResults", e);
                throw new WrongXPathForTypeException("Error when navigating through XPathResults");
            }
        } catch (ParseException e) {
            log.error("Error when parsing result of XPathSearch", e);
            throw new WrongXPathForTypeException("Error when parsing result of XPathSearch");
        }
    }

    @Override
    public String asString() {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try {
            navigation.dumpXML(byteOutputStream);
        } catch (IOException e) {
            log.error("Error when dumping xml state");
        }
        return byteOutputStream.toString();
    }

//
//    /**
//     * Used to get all Attributes of the Current Node.
//     *
//     * @return A List of all XMLAttributes in the current Node.
//     * @throws NavException When an Error occurs navigating the attributes;
//     */
//    private List<XMLAttribute> evaluateAttributes() throws NavException {
//        final List<XMLAttribute> attributes = new ArrayList<>();
//        int attrCount = navigation.getAttrCount();
//        for (int i = navigation.getCurrentIndex() + 1; i <= navigation.getCurrentIndex() + 1 + attrCount; i += 2) {
//            String attributeKey = navigation.toNormalizedString(i);
//            String attributeValue = navigation.toNormalizedString(i + 1);
//            attributes.add(new XMLAttribute(attributeKey, attributeValue));
//        }
//        return attributes;
//    }
//
//
//    /**
//     * Used to get All Children of the Current Node as Well as its Siblings. Uses Recursive DFS to find all nodes required then parses them into XMLElements
//     * and returns as List of Lists with the first element being the children and the second Element being the siblings (each as its own XMLElement List).
//     *
//     * @param rootIndex   the rootIndex index we start the search from.
//     * @param parentIndex the parent Index of the current search.
//     * @param startDepth  the startDepth of the current iteration.
//     * @return A List of Lists with 2 elements: Element at index 0 is the children of the current Node. Element at index 1 is the siblings of the current node.
//     * @throws NavException Error that occurs when the traversing of Nodes fails.
//     */
//    private List<List<XMLElement>> evaluateChildrenAndSiblings(int rootIndex, int parentIndex, int startDepth) throws NavException {
//        List<XMLElement> children = new ArrayList<>();
//        List<XMLElement> siblings = new ArrayList<>();
//        List<List<XMLElement>> result = new ArrayList<>();
//        result.add(children);
//        result.add(siblings);
//        //Traversing children, uses startDepth as a check as we don't need to traverse already visited child nodes.
//        if (navigation.toElement(VTDNav.FIRST_CHILD) && startDepth < navigation.getCurrentDepth()) {
//            int curIndex = navigation.getCurrentIndex();
//            XMLElement child = new XMLElement(navigation.toNormalizedString(curIndex));
//            child.getAttributes().addAll(evaluateAttributes());
//            List<List<XMLElement>> newChildren = evaluateChildrenAndSiblings(rootIndex, curIndex, startDepth + 1);
//            child.getChildren().addAll(newChildren.get(0));
//            children.add(child);
//            children.addAll(newChildren.get(1));
//        }
//        //After traversing all children nodes we now go back to our parent element and traverse all our siblings.
//        navigation.recoverNode(parentIndex);
//        //Assignment so the next sibling traversal uses correct depth to determine if we should search for more children.
//        startDepth--;
//        //Traversing siblings, uses rootIndex in the check so we don't keep on checking siblings of the node we start our search from.
//        if (navigation.toElement(VTDNav.NEXT_SIBLING) && rootIndex < navigation.getCurrentDepth()) {
//            int curIndex = navigation.getCurrentIndex();
//            XMLElement sibling = new XMLElement(navigation.toNormalizedString(curIndex));
//            sibling.getAttributes().addAll(evaluateAttributes());
//            List<List<XMLElement>> newChildren = evaluateChildrenAndSiblings(rootIndex, curIndex, startDepth + 1);
//            sibling.getChildren().addAll(newChildren.get(0));
//            siblings.add(sibling);
//            siblings.addAll(newChildren.get(1));
//        }
//        return result;
//    }
//
//    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ParseException, IOException, WrongXPathForTypeException {
//        Context foo = Fralax.parse("foo");
//        try {
//            List<Context> contexts = foo.selectAll("//element/text()");
//            Collection<String> collect = contexts.stream().map(Context::asString).collect(Collectors.toCollection());
//        } catch (WrongXPathForTypeException e) {
//            e.printStackTrace();
//        }
//    }

}
