package net.oneandone.fralax.model;

import net.onenandone.fralax.model.ListOfXMLElements;
import net.onenandone.fralax.model.XMLAttribute;
import net.onenandone.fralax.model.XMLElement;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 05.04.16.
 * @version 1.0
 * Tests the ListOfXMLElementsTest
 */
public class ListOfXMLElementsTest {
    private ListOfXMLElements listOfXMLElements = new ListOfXMLElements(new ArrayList<>());
    private XMLElement test = new XMLElement("driver");
    private XMLElement cars = new XMLElement("cars");
    private XMLElement audi = new XMLElement("audi");
    private XMLElement bmw = new XMLElement("bmw");
    private XMLElement volvo = new XMLElement("volvo");
    private XMLAttribute name = new XMLAttribute("name", "fralax");
    private XMLAttribute kind = new XMLAttribute("kind", "car");
    private XMLAttribute carSeries = new XMLAttribute("series", "123X");
    private XMLAttribute newCar = new XMLAttribute("new", "true");

    @Before
    /**
     * Sets up required Objects.
     */
    public void setUp() {
        audi.getAttributes().add(carSeries);
        audi.getAttributes().add(newCar);
        bmw.getAttributes().add(carSeries);
        bmw.getAttributes().add(newCar);
        volvo.getAttributes().add(carSeries);
        cars.getChildren().add(audi);
        cars.getChildren().add(bmw);
        cars.getChildren().add(volvo);
        test.getAttributes().add(name);
        test.getAttributes().add(kind);
        listOfXMLElements.getElementList().add(test);
    }

    @Test
    /**
     * Tests search both for one and multiple objects
     */
    public void testSearchForElement() {
        Optional<XMLElement> opt = listOfXMLElements.getFirstElement("name", "fralax");
        if (opt.isPresent()) {
            assertEquals(test, opt.get());
        }
        else {
            fail();
        }
        opt = listOfXMLElements.getFirstElement("name", "fralax123");
        assertTrue(!opt.isPresent());
        listOfXMLElements = new ListOfXMLElements(new ArrayList<>());
        listOfXMLElements.getElementList().add(audi);
        listOfXMLElements.getElementList().add(bmw);
        listOfXMLElements.getElementList().add(volvo);
        ListOfXMLElements searchResult = listOfXMLElements.getAllElements("series", "123X");
        assertEquals(searchResult.getElementList().size(), 3);
        searchResult = listOfXMLElements.getAllElements("series", "XX-X");
        assertEquals(searchResult.getElementList().size(), 0);
        searchResult = listOfXMLElements.getAllElements("new", "true");
        assertEquals(searchResult.getElementList().size(), 2);
    }

}
