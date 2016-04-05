package net.oneandone.fralax.parser;

import net.onenandone.fralax.parser.XPathType;
import org.junit.Test;
import static net.onenandone.fralax.parser.XMLParser.evaluateXPath;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 05.04.16.
 * @version 1.0
 */
public class XMLParserTest {

    @Test
    /**
     * Test for the static XPathEvaluation function.
     */
    public void testEvaluationMethod() throws Exception {
        assertEquals(XPathType.MULTIPLE_ELEMENTS, evaluateXPath("/book"));
        assertEquals(XPathType.MULTIPLE_ELEMENTS,evaluateXPath("//router"));
        assertEquals(XPathType.MULTIPLE_ELEMENTS, evaluateXPath("/routerset"));
        assertEquals(XPathType.ELEMENT, evaluateXPath("//router[@id='123.5.1.2.3.4']"));
        assertEquals(XPathType.ELEMENT,evaluateXPath("//ns:router[@id='123']"));
        assertEquals(XPathType.INVALID, evaluateXPath("//.:"));
        assertEquals(XPathType.ELEMENT, evaluateXPath("/driver/hello[@x='123123']"));
        assertEquals(XPathType.MULTIPLE_ELEMENTS, evaluateXPath("/driver/*"));
        assertEquals(XPathType.MULTIPLE_ELEMENTS, evaluateXPath("/driver[@*]"));
        assertEquals(XPathType.MULTIPLE_ATTRIBUTES, evaluateXPath("//@lang"));
        assertEquals(XPathType.MULTIPLE_ELEMENTS, evaluateXPath("/bookstore/book/title | //price"));
        assertEquals(XPathType.MULTIPLE_ELEMENTS, evaluateXPath("/bookstore/book[price>35.00]"));
        assertEquals(XPathType.MULTIPLE_ATTRIBUTES, evaluateXPath("@*"));
        assertEquals(XPathType.MULTIPLE_ELEMENTS, evaluateXPath("//sn:router[@id='gw-ps5.slr.lxa.us']/sn:interface"));
        assertEquals(XPathType.ATTRIBUTE, evaluateXPath("@id='123'"));
    }

}
