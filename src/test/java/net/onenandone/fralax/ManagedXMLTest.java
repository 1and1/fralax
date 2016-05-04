package net.onenandone.fralax;

import net.onenandone.fralax.parser.VtdXmlParser;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 04.05.16.
 * @version 1.0
 */
public class ManagedXMLTest  {

    private static ManagedXmlParser xmlParser;
    private static XmlContext xml;

    @BeforeClass
    public static void setUpClass() throws Exception {
        xmlParser = Fralax.parseAndManage(FralaxTest.class.getResource("/driverVehicleInfo.xml").getFile(), VtdXmlParser.class);
        xml = xmlParser.getRootContext();
    }

    @Test
    public void testManagement() throws Exception {
        assertEquals(xml.selectAll("//driver").size(), xmlParser.getRootContext().selectAll("//driver").size());

        //Now Change the file
        BufferedWriter writer = new BufferedWriter(new FileWriter(FralaxTest.class.getResource("/driverVehicleInfo.xml").getFile()));
        //Remove the RR1 vehicle
        writer.write("<driverVehicleInfo>\n" +
                "    <vehicle id=\"AM1\">\n" +
                "        <vehicleId>2</vehicleId>\n" +
                "        <name>Aston Martin</name>\n" +
                "    </vehicle>\n" +
                "    <vehicle id=\"B1\">\n" +
                "        <vehicleId>3</vehicleId>\n" +
                "        <name>Bus</name>\n" +
                "    </vehicle>\n" +
                "\n" +
                "    <driver>\n" +
                "        <driverId>1</driverId>\n" +
                "        <firstName>John</firstName>\n" +
                "        <lastName>Doe</lastName>\n" +
                "        <vehicleId>1</vehicleId>\n" +
                "        <vehicleId>2</vehicleId>\n" +
                "    </driver>\n" +
                "    <driver>\n" +
                "        <driverId>2</driverId>\n" +
                "        <name>Joe Blocks</name>\n" +
                "        <vehicleId>3</vehicleId>\n" +
                "    </driver>\n" +
                "</driverVehicleInfo>");
        writer.flush();
        writer.close();
        //This is still the old XML Context
        Optional<XmlContext> oneVehicle = xml.select("//vehicle[@id='RR1']");
        assertTrue(oneVehicle.isPresent());
        assertEquals("<vehicle id=\"RR1\">\n" +
                "    <vehicleId>1</vehicleId>\n" +
                "    <name>Limousine</name>\n" +
                "</vehicle>", oneVehicle.get().asString(true));
        //Now check if change happens when we call getRootContext
        Optional<XmlContext> newContext = xmlParser.getRootContext().select("//vehicle[@id='RR1']");
        //same xpath but now we updated so it isn't present anymore.
        assertFalse(newContext.isPresent());
    }

    @Test
    public void testCount() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("count(/driverVehicleInfo/vehicle)");
        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());

        assertEquals("3", optionalContext.get().asString());
    }

    @Test
    public void testSelectSingleElement() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/driverVehicleInfo/vehicle[@id='RR1']");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
    }

    @Test(expected = FralaxException.class)
    public void testSelectMultipleElementsForSingleSelect() throws Exception {
        xml.select("/driverVehicleInfo/vehicle");
    }

    @Test
    public void testSelectElementWithinContext() throws Exception {
        Optional<XmlContext> optionalContext = xml.select("/driverVehicleInfo/vehicle[@id='RR1']");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());

        optionalContext = optionalContext.get().select("name/text()");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("Limousine", optionalContext.get().asString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSubsequentSelectFromValue() throws Exception {
        //noinspection OptionalGetWithoutIsPresent
        xml.select("/driverVehicleInfo/vehicle[@id='AM1']/@id").get().select("/anything");
    }

    @Test
    public void testSelectListOfElements() throws Exception {
        final List<XmlContext> contexts = xml.selectAll("/driverVehicleInfo/vehicle");

        assertNotNull(contexts);
        assertEquals(3, contexts.size());
    }

    @Test
    public void testSelectNonExistingElement() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/driverVehicleInfo/vehicle[@id='Missing Element']");

        assertNotNull(optionalContext);
        assertFalse(optionalContext.isPresent());
    }

    @Test
    public void testSelectAttributeToString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/driverVehicleInfo/vehicle[@id='AM1']/@id");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("AM1", optionalContext.get().asString());
    }

    @Test
    public void testSelectElementValueToString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/driverVehicleInfo/vehicle[@id='B1']/name/text()");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("Bus", optionalContext.get().asString());
    }

    @Test
    public void testElementToUnformattedString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/driverVehicleInfo/driver[1]");
        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("<driver><driverId>1</driverId><firstName>John</firstName><lastName>Doe</lastName>" +
                        "<vehicleId>1</vehicleId><vehicleId>2</vehicleId></driver>",
                optionalContext.get().asString()
        );
    }

    @Test
    public void testElementToFormattedString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/driverVehicleInfo/driver[1]");
        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("<driver>\n" +
                        "    <driverId>1</driverId>\n" +
                        "    <firstName>John</firstName>\n" +
                        "    <lastName>Doe</lastName>\n" +
                        "    <vehicleId>1</vehicleId>\n" +
                        "    <vehicleId>2</vehicleId>\n" +
                        "</driver>",
                optionalContext.get().asString(true)
        );
    }


    @Test
    public void testSelectListOfAttributes() throws Exception {
        final List<XmlContext> contexts = xml.selectAll("//@id");
        assertEquals(3, contexts.size());
        assertEquals("RR1", contexts.get(0).asString(true));
        assertEquals("AM1", contexts.get(1).asString(true));
        assertEquals("B1", contexts.get(2).asString(true));
    }

    @Test(expected = FralaxException.class)
    public void testSelectBinaryExpression() throws Exception {
        xml.selectAll("@id='RR1'");
    }

}
