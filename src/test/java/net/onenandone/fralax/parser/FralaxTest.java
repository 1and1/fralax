package net.onenandone.fralax.parser;

import net.onenandone.fralax.model.Context;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class FralaxTest {

    private Context xml;

    @Before
    public void setUp() throws Exception {
        xml = Fralax.parse(FralaxTest.class.getResource("/driverVehicleInfo.xml").getFile());
    }

    @Test
    public void testSelectSingleElement() throws Exception {
        final Optional<Context> optionalContext = xml.select("/driverVehicleInfo/vehicle[@id='RR1']");
        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
    }

    @Test
    public void testSelectListOfElements() throws Exception {
        final List<Context> contexts = xml.selectAll("/driverVehicleInfo/vehicle");

        assertNotNull(contexts);
        assertEquals(3, contexts.size());
    }

    @Test
    public void testSelectNonExistingElement() throws Exception {
        final Optional<Context> optionalContext = xml.select("/driverVehicleInfo/vehicle[@id='Missing Element']");

        assertNotNull(optionalContext);
        assertFalse(optionalContext.isPresent());
    }

    @Test
    public void testSelectAttributeToString() throws Exception {
        final Optional<Context> optionalContext = xml.select("/driverVehicleInfo/vehicle[@id='AM1']/@id");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("AM1", optionalContext.get().asString());
    }

    @Test
    public void testSelectElementValueToString() throws Exception {
        final Optional<Context> optionalContext = xml.select("/driverVehicleInfo/vehicle[@id='B1']/name/text()");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("Bus", optionalContext.get());
    }

    @Test
    public void testElementToString() throws Exception {
        final Optional<Context> optionalContext = xml.select("/driverVehicleInfo/driver[1]");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals(
                "<driver>\n" +
                "   <driverId>1</driverId>\n" +
                "   <firstName>John</firstName>\n" +
                "   <lastName>Doe</lastName>\n" +
                "   <vehicleId>1</vehicleId>\n" +
                "   <vehicleId>2</vehicleId>\n" +
                "</driver>",
                optionalContext.get()
        );
    }
}