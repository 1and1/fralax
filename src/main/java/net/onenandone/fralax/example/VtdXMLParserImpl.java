package net.onenandone.fralax.example;

import com.ximpleware.*;
import net.onenandone.fralax.model.ListOfXMLAttributes;
import net.onenandone.fralax.model.ListOfXMLElements;
import net.onenandone.fralax.model.XMLAttribute;
import net.onenandone.fralax.model.XMLElement;
import net.onenandone.fralax.parser.XMLParser;
import net.onenandone.fralax.parser.XMLParserDefaultFactory;

import javax.xml.xpath.XPathException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 */
public class VtdXMLParserImpl extends XMLParser {

    private AutoPilot ap;
    private VTDNav vn;

    public VtdXMLParserImpl(File fileToParse, Map<String, String> namespaces) throws IOException, ParseException {
        super(fileToParse, namespaces);
        FileInputStream fis = null;
        fis = new FileInputStream(fileToParse);
        byte[] xmlByteArray = new byte[(int) fileToParse.length()];
        if (fis.read(xmlByteArray) != xmlByteArray.length) {
            throw new IOException("Error when reading the XML File");
        }
        final VTDGen vg = new VTDGen();
        vg.setDoc(xmlByteArray);
        vg.parse(true); // set namespace awareness to true
        vn = vg.getNav();
        ap = new AutoPilot(vn);
        for (Map.Entry<String, String> namespace : namespaces.entrySet()) {
            ap.declareXPathNameSpace(namespace.getKey(), namespace.getValue());
        }
    }

    @Override
    protected ListOfXMLAttributes searchForAllAttributes(String xPathQuery) {
        return null;
    }

    @Override
    protected XMLAttribute searchForAttribute(String xPathQuery) {
        return null;
    }

    @Override
    protected XMLElement searchForElement(String xPathQuery) {
        try {
            ap.selectXPath(xPathQuery);
        } catch (XPathParseException e) {
            //Exception would have been handled in XMLParser
        }
        try {
            int res = ap.evalXPath();
            int curDepth = vn.getCurrentDepth();
            System.out.println("curDepth = " + curDepth);
            if (res != -1) {
                XMLElement result = new XMLElement(vn.toNormalizedString(res));
                List<XMLAttribute> attributes = evaluateAttributes();
                result.getAttributes().addAll(attributes);
                List<XMLElement> children = evaluateChildren(curDepth);
                result.getChildren().addAll(children);
                return result;
            }
        } catch (XPathEvalException | NavException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<XMLAttribute> evaluateAttributes() throws NavException {
        final List<XMLAttribute> attributes = new ArrayList<>();
        vn.recoverNode(vn.getCurrentIndex());
        int attrCount = vn.getAttrCount();
        for (int i = vn.getCurrentIndex() + 1; i <= vn.getCurrentIndex() + 1 + attrCount; i+=2) {
            String attributeKey = vn.toNormalizedString(i);
            String attributeValue = vn.toNormalizedString(i+1);
            attributes.add(new XMLAttribute(attributeKey, attributeValue));
        }
        return attributes;
    }

    private List<XMLElement> evaluateChildren(final int startDepth) throws NavException {
        List<XMLElement> children = new ArrayList<>();
        vn.recoverNode(vn.getCurrentIndex());
        while(vn.toElement(VTDNav.FIRST_CHILD) && startDepth < vn.getCurrentDepth()) {
            XMLElement child = new XMLElement(vn.toNormalizedString(vn.getCurrentIndex()));
            child.getAttributes().addAll(evaluateAttributes());
            child.getChildren().addAll(evaluateChildren(startDepth + 1));
            children.add(child);
        }
        vn.toElement(VTDNav.PARENT);
        while(vn.toElement(VTDNav.NEXT_SIBLING)  && startDepth < vn.getCurrentDepth()) {
            System.out.println("hello = " );
            XMLElement sibling = new XMLElement(vn.toNormalizedString(vn.getCurrentIndex()));
            sibling.getAttributes().addAll(evaluateAttributes());
            sibling.getChildren().addAll(evaluateChildren(startDepth + 1));
            children.add(sibling);
        }
        return children;

    }


    @Override
    protected ListOfXMLElements searchForAllElements(String xPathQuery) {
        return null;
    }

    public static void main(String args[]) {
        HashMap<String, String> h= new HashMap<>();
        h.put("sn", "http://www.1und1.de/n-config.xml");
        XMLParser parser = new XMLParserDefaultFactory().createNewInstance("src/main/resources/test/n-config.xml", h);
        try {
            System.out.println(((XMLElement)parser.searchFor("//sn:router[@id='sw-bc-ps-1-1.4b.r221.bap.rhr.de']").get()).prettyPrint());
        } catch (XPathException e) {
            e.printStackTrace();
        }
    }

}
