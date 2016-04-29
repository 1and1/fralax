package net.onenandone.fralax;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class FralaxTest {

    private static XmlContext xml;
    private static String file;

    @BeforeClass
    public static void setUp() throws Exception {
        file = FralaxTest.class.getResource("/driverVehicleInfo.xml").getFile();
        xml = Fralax.parse(file);
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
                        "\t<driverId>1</driverId>\n" +
                        "\t<firstName>John</firstName>\n" +
                        "\t<lastName>Doe</lastName>\n" +
                        "\t<vehicleId>1</vehicleId>\n" +
                        "\t<vehicleId>2</vehicleId>\n" +
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

    @Test
    public void testWasChangedOnce() throws Exception {
        assertFalse(((XmlRootContext) xml).wasChanged());
        //We wait as to make sure the system calls for the lastModified date are actually changed. In production,
        //this just means, that a change is not registered immediately as we have to wait for the OS to write
        //last modified date.
        Thread.sleep(1000);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(xml.asString(true));
        bw.flush();
        bw.close();
        Thread.sleep(500);
        assertTrue(((XmlRootContext) xml).wasChanged());
    }

    @Test
    public void testAutoUpdate() throws Exception {
        XmlContext original = xml;
        xml = Fralax.parse(file);
        assertFalse(((XmlRootContext) xml).wasChanged());
        //We wait as to make sure the system calls for the lastModified date are actually changed. In production,
        //this just means, that a change is not registered immediately as we have to wait for the OS to write
        //last modified date.
        Thread.sleep(1000);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(xml.asString(true));
        bw.flush();
        bw.close();
        ((XmlRootContext) xml).startAutoUpdate();
        Thread.sleep(500);
        //Since AutoUpdater is now updating the context automatically, this function will only be true during the the time span when
        //the autoupdate hasn't processed the update yet.
        assertFalse(((XmlRootContext) xml).wasChanged());

        assertFalse(xml.select("//vehicle[@id='O1']").isPresent());
        bw = new BufferedWriter(new FileWriter(file));
        bw.write("<driverVehicleInfo>\n" +
                "    <vehicle id=\"RR1\">\n" +
                "        <vehicleId>1</vehicleId>\n" +
                "        <name>Limousine</name>\n" +
                "    </vehicle>\n" +
                "    <vehicle id=\"AM1\">\n" +
                "        <vehicleId>2</vehicleId>\n" +
                "        <name>Aston Martin</name>\n" +
                "    </vehicle>\n" +
                "    <vehicle id=\"B1\">\n" +
                "        <vehicleId>3</vehicleId>\n" +
                "        <name>Bus</name>\n" +
                "    </vehicle>\n" +
                "    <vehicle id=\"O1\">\n" +
                "        <vehicleId>4</vehicleId>\n" +
                "        <name>Opel Astra</name>\n" +
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
        bw.flush();
        bw.close();
        //wait for AutoUpdater to actually get changed file date from os.
        Thread.sleep(2000);
        Optional<XmlContext> opel = xml.select("//vehicle[@id='O1']");
        if (opel.isPresent()) {
            assertEquals("<vehicle id=\"O1\">\n" +
                            "\t<vehicleId>4</vehicleId>\n" +
                            "\t<name>Opel Astra</name>\n" +
                            "</vehicle>",
                    opel.get().asString(true));

        }
        else {
            fail("AutoUpdate failed");
        }
        ((XmlRootContext) xml).stopAutoUpdate();
        //Use old xml to continue parsing with correct data.
        xml = original;
    }

}