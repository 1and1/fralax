package net.oneandone.fralax.example;

import net.onenandone.fralax.model.ListOfXMLAttributes;
import net.onenandone.fralax.model.XMLElement;
import net.onenandone.fralax.model.XPathResult;
import net.onenandone.fralax.parser.XMLParserDefaultFactory;
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



    /*public static void main(String args[]) {
        HashMap<String, String> h = new HashMap<>();
        h.put("sn", "http://www.1und1.de/n-config.xml");
        XMLParserFactory fact = new XMLParserDefaultFactory();
        XPathParser parser = fact.createNewInstance("src/main/resources/test/n-config.xml", h);
       /* try {
            for (XMLElement e : ((ListOfXMLElements)parser.searchFor("//sn:router/sn:interface").get()).getElementList()) {
                System.out.println(e.prettyPrint());
            }
        } catch (XPathException e) {
            e.printStackTrace();
        }
        try {
            for (XMLElement e : ((ListOfXMLElements)parser.searchFor("//sn:router[@id='gw-ps5.slr.lxa.us']//sn:interface").get()).getElementList()) {
                System.out.println(e.prettyPrint());
            }
        }
         catch (Exception e) {
            e.printStackTrace();
        }
      /*  try {
            for (XMLAttribute attribute : ((ListOfXMLAttributes) (parser.searchFor("//router[@id='usva3a.bap.glt']/@id").get())).getAttributeList()) {
                System.out.println(attribute.prettyPrint());
            }

        } catch (XPathException e) {
            e.printStackTrace();
        }
       /* try {
            System.out.println(((XMLAttribute)parser.searchFor("//router/@id").get()).prettyPrint());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }*/






}
