package net.onenandone.fralax;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class FralaxQualifiedNamespaceTest {

    private static XmlContext xml;
    private static String file;

    @BeforeClass
    public static void setUp() throws Exception {
        file = FralaxTest.class.getResource("/books-qualified.xml").getFile();
        xml = Fralax.parse(file);
    }

    @Test
    public void setNoSelectToString() throws Exception {
        assertEquals(
                "<b:books xmlns:b=\"urn:books:qualified\">\n" +
                        "\t<b:book id=\"bk001\">\n" +
                        "\t\t<b:author>Writer</b:author>\n" +
                        "\t\t<b:title>The First Book</b:title>\n" +
                        "\t\t<b:genre>Fiction</b:genre>\n" +
                        "\t\t<b:price>44.95</b:price>\n" +
                        "\t\t<b:pub_date>2000-10-01</b:pub_date>\n" +
                        "\t\t<b:review>An amazing story of nothing.</b:review>\n" +
                        "\t</b:book>\n" +
                        "\t<b:book id=\"bk002\">\n" +
                        "\t\t<b:author>Poet</b:author>\n" +
                        "\t\t<b:title>The Poet's First Poem</b:title>\n" +
                        "\t\t<b:genre>Poem</b:genre>\n" +
                        "\t\t<b:price>24.95</b:price>\n" +
                        "\t\t<b:review>Least poetic poems.</b:review>\n" +
                        "\t</b:book>\n" +
                        "</b:books>",
                xml.asString(true)
        );
    }

    @Test
    public void testSelectSingleElement() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/b:book[@id='bk001']");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
    }

    @Test(expected = FralaxException.class)
    public void testSelectMultipleElementsForSingleSelect() throws Exception {
        xml.select("/b:books/b:book");
    }

    @Test
    public void testSelectElementWithinContext() throws Exception {
        Optional<XmlContext> optionalContext = xml.select("/b:books/b:book[@id='bk002']");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());

        optionalContext = optionalContext.get().select("b:genre/text()");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("Poem", optionalContext.get().asString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSubsequentSelectFromValue() throws Exception {
        //noinspection OptionalGetWithoutIsPresent
        xml.select("/b:books/b:book[@id='bk001']/@id").get().select("/anything");
    }

    @Test
    public void testSelectListOfElements() throws Exception {
        final List<XmlContext> contexts = xml.selectAll("/b:books/b:book");

        assertNotNull(contexts);
        assertEquals(2, contexts.size());
    }

    @Test
    public void testSelectNonExistingElement() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/book[@id='Missing Element']");

        assertNotNull(optionalContext);
        assertFalse(optionalContext.isPresent());
    }

    @Test
    public void testSelectAttributeToString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/b:book[@id='bk002']/@id");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("bk002", optionalContext.get().asString());
    }

    @Test
    public void testSelectElementValueToString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/b:book[@id='bk001']/b:genre/text()");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("Fiction", optionalContext.get().asString());
    }

    @Test
    public void testElementToUnformattedString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/b:book[2]");
        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("<b:book id=\"bk002\"><b:author>Poet</b:author><b:title>The Poet's First Poem</b:title><b:genre>Poem</b:genre><b:price>24.95</b:price><b:review>Least poetic poems.</b:review></b:book>",
                optionalContext.get().asString()
        );
    }

    @Test
    public void testElementToFormattedString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/b:book[1]");
        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("<b:book id=\"bk001\">\n" +
                        "\t<b:author>Writer</b:author>\n" +
                        "\t<b:title>The First Book</b:title>\n" +
                        "\t<b:genre>Fiction</b:genre>\n" +
                        "\t<b:price>44.95</b:price>\n" +
                        "\t<b:pub_date>2000-10-01</b:pub_date>\n" +
                        "\t<b:review>An amazing story of nothing.</b:review>\n" +
                        "</b:book>",
                optionalContext.get().asString(true)
        );
    }

    @Test
    public void testSelectListOfAttributes() throws Exception {
        final List<XmlContext> contexts = xml.selectAll("//@id");
        assertEquals(2, contexts.size());
        assertEquals("bk001", contexts.get(0).asString(true));
        assertEquals("bk002", contexts.get(1).asString(true));
    }

    @Test(expected = FralaxException.class)
    public void testSelectBinaryExpression() throws Exception {
        xml.selectAll("@id='RR1'");
    }

    @Test
    public void testSelectMutiple() throws Exception {
        final List<XmlContext> contexts = xml.selectAll("//b:author | //b:title");
        assertFalse(contexts.isEmpty());
        assertEquals("<b:author>Writer</b:author>",
                contexts.get(0).asString(true));
        assertEquals("<b:author>Poet</b:author>",
                contexts.get(1).asString(true));
        assertEquals("<b:title>The First Book</b:title>",
                contexts.get(2).asString(true));
        assertEquals("<b:title>The Poet's First Poem</b:title>",
                contexts.get(3).asString(true));

    }

    @Test
    public void testWasChangedOnce() throws Exception {
        assertFalse(((XmlRootContext)xml).wasChanged());
        //We wait as to make sure the system calls for the lastModified date are actually changed. In production,
        //this just means, that a change is not registered immediately as we have to wait for the OS to write
        //last modified date.
        Thread.sleep(1000);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(xml.asString(true));
        bw.flush();
        bw.close();
        Thread.sleep(500);
        assertTrue(((XmlRootContext)xml).wasChanged());
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

        assertFalse(xml.select("//b:book[@id='bk003']").isPresent());
        bw = new BufferedWriter(new FileWriter(file));
        bw.write("<?xml version=\"1.0\"?>\n" +
                "<b:books xmlns:b=\"urn:books:unqualified\">\n" +
                "    <b:book id=\"bk001\">\n" +
                "        <b:author>Writer</b:author>\n" +
                "        <b:title>The First Book</b:title>\n" +
                "        <b:genre>Fiction</b:genre>\n" +
                "        <b:price>44.95</b:price>\n" +
                "        <b:pub_date>2000-10-01</b:pub_date>\n" +
                "        <b:review>An amazing story of nothing.</b:review>\n" +
                "    </b:book>\n" +
                "\n" +
                "    <b:book id=\"bk002\">\n" +
                "        <b:author>Poet</b:author>\n" +
                "        <b:title>The Poet's First Poem</b:title>\n" +
                "        <b:genre>Poem</b:genre>\n" +
                "        <b:price>24.95</b:price>\n" +
                "        <b:review>Least poetic poems.</b:review>\n" +
                "    </b:book>\n" +
                "\n" +
                "    <b:book id=\"bk003\">\n" +
                "        <b:author>Edgar Allan Poe</b:author>\n" +
                "        <b:title>HELLO</b:title>\n" +
                "        <b:genre>Sci-Fi</b:genre>\n" +
                "        <b:price>15.95</b:price>\n" +
                "        <b:review>VERY GOOD 5/7</b:review>\n" +
                "    </b:book>\n" +
                "</b:books>");
        bw.flush();
        bw.close();
        long currentTime = System.currentTimeMillis();
        boolean present = false;
        while(System.currentTimeMillis() - currentTime <= 1000000 && !present) {
            //Try check again in 1000 ms.
            Thread.sleep(3000);
            present = xml.select("//b:book[@id='bk003']").isPresent();
        }
        Optional<XmlContext> hello = xml.select("//b:book[@id='bk003']");
        if (hello.isPresent()) {
            assertEquals("<b:book id=\"bk003\">\n" +
                            "\t<b:author>Edgar Allan Poe</b:author>\n" +
                            "\t<b:title>HELLO</b:title>\n" +
                            "\t<b:genre>Sci-Fi</b:genre>\n" +
                            "\t<b:price>15.95</b:price>\n" +
                            "\t<b:review>VERY GOOD 5/7</b:review>\n" +
                            "</b:book>",
                    hello.get().asString(true));
        }
        else {
            ((XmlRootContext) xml).stopAutoUpdate();
            xml = original;
            fail("AutoUpdate failed");
        }
        //Use old xml to continue parsing with correct data.
        ((XmlRootContext) xml).stopAutoUpdate();
        xml = original;
    }


}

