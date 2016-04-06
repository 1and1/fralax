package net.onenandone.fralax;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class FralaxTest {

    private XmlContext xml;

    @Before
    public void setUp() throws Exception {
        xml = Fralax.parse(FralaxTest.class.getResource("/driverVehicleInfo.xml").getFile());
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

        optionalContext = optionalContext.get().select("/vehicle/name/text()");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("Limousine", optionalContext.get().asString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSubsequentSelectFromValue() throws Exception {
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
                        "  <driverId>1</driverId>\n" +
                        "  <firstName>John</firstName>\n" +
                        "  <lastName>Doe</lastName>\n" +
                        "  <vehicleId>1</vehicleId>\n" +
                        "  <vehicleId>2</vehicleId>\n" +
                        "</driver>\n",
                optionalContext.get().asFormattedString()
        );
    }
}