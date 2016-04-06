package net.oneandone.fralax.example;

import net.onenandone.fralax.parser.XPathParser;
import org.junit.Before;
import org.junit.Test;

import javax.xml.xpath.XPathException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 05.04.16.
 * @version 1.0
 */
public class VtdXMLParserTest {

    private XPathParser parser;


    @Before
    /**
     * Sets up necessary Objects.
     */
    public void setUp() {
        parser = XMLParserDefaultFactory.createNewInstance("src/test/resources/driverVehicleInfo.xml");
    }

    @Test
    public void testSearch() throws XPathException {
            Optional<XPathResult> result = parser.searchFor("//vehicle[@id='RR1']");
            if (result.isPresent() && XMLElement.class.isAssignableFrom(result.get().getClass())) {
                XMLElement elementResult = (XMLElement)result.get();
                assertTrue(elementResult.getAttribute("id").isPresent() && elementResult.getAttribute("id").get().getValue().equals("RR1"));
                assertEquals(elementResult.getIdentifier(), "vehicle");
                assertEquals(elementResult.getChildren().size(), 2);
                assertTrue(elementResult.getChild("name").isPresent());
            }
            else {
                fail("Wrong XPathResult type returned!");
            }
            result = parser.searchFor("//@id");
            if (result.isPresent() && ListOfXMLAttributes.class.isAssignableFrom(result.get().getClass())) {
                ListOfXMLAttributes attributeListResult = (ListOfXMLAttributes)result.get();
                assertEquals(attributeListResult.getAttributeList().size(), 3);
            }

    }
}
