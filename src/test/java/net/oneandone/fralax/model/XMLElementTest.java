package net.oneandone.fralax.model;

import net.onenandone.fralax.model.XMLAttribute;
import net.onenandone.fralax.model.XMLElement;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 05.04.16.
 * @version 1.0
 * Tests the XMLElement Class.
 */
public class XMLElementTest {

    private XMLElement test = new XMLElement("driver");
    private XMLElement cars = new XMLElement("cars");
    private XMLElement audi = new XMLElement("audi");
    private XMLElement bmw = new XMLElement("bmw");
    private XMLElement volvo = new XMLElement("volvo");
    private XMLAttribute name = new XMLAttribute("name", "fralax");
    private XMLAttribute kind = new XMLAttribute("kind", "car");

    @Before
    /**
     * Sets up the required Testdata.
     */
    public void setUp() {
        cars.getChildren().add(audi);
        cars.getChildren().add(bmw);
        cars.getChildren().add(volvo);
        test.getAttributes().add(name);
        test.getAttributes().add(kind);
    }


    @Test
    /**
     * Tests the Attribute Search of a XMLElement.
     */
    public void testAttributeSearch() {
        Optional<XMLAttribute> opt = test.getAttribute("kind");
        if (opt.isPresent()) {
            assertEquals(kind, opt.get());
        }
        else {
            fail();
        }
        opt = test.getAttribute("name");
        if (opt.isPresent()) {
            assertEquals(name, opt.get());
        }
        else {
            fail();
        }
        opt = test.getAttribute("attrib");
        assertFalse(opt.isPresent());

    }

    @Test
    /**
     * Tests the Element Search of a XMLElement.
     */
    public void testElementSearch() {
        Optional<XMLElement> opt = cars.getChild("audi");
        if (opt.isPresent()) {
            assertEquals(audi, opt.get());
        }
        opt = cars.getChild("bmw");
        if (opt.isPresent()) {
            assertEquals(bmw, opt.get());
        }
        opt = cars.getChild("volvo");
        if (opt.isPresent()) {
            assertEquals(volvo, opt.get());
        }
        opt = cars.getChild("mercedes");
        assertFalse(opt.isPresent());
    }


    @Test
    /**
     * Tests the Printing mechanism of a XMLElement
     */
    public void testPrettyPrint() {
        String carsPrettyPrint = cars.prettyPrint();
        assertTrue(carsPrettyPrint.endsWith("END XMLELEMENT\n" +
                "---------------------------\n"));
        assertTrue(carsPrettyPrint.split("END ").length == 5);
        assertTrue(carsPrettyPrint.contains("audi") &&  carsPrettyPrint.contains("bmw") && carsPrettyPrint.contains("volvo") && carsPrettyPrint.contains("cars"));
    }


}
