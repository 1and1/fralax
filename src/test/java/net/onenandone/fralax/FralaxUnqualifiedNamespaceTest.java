package net.onenandone.fralax;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class FralaxUnqualifiedNamespaceTest {

    private static XmlContext xml;
    private static String file;


    @BeforeClass
    public static void setUp() throws Exception {
        file = FralaxTest.class.getResource("/books-unqualified.xml").getFile();
        xml = Fralax.parse(file);
    }

    @Test
    public void testSelectSingleElement() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/book[@id='bk001']");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
    }

    @Test(expected = FralaxException.class)
    public void testSelectMultipleElementsForSingleSelect() throws Exception {
        xml.select("/b:books/book");
    }

    @Test
    public void testSelectElementWithinContext() throws Exception {
        Optional<XmlContext> optionalContext = xml.select("/b:books/book[@id='bk002']");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());

        optionalContext = optionalContext.get().select("genre/text()");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("Poem", optionalContext.get().asString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSubsequentSelectFromValue() throws Exception {
        //noinspection OptionalGetWithoutIsPresent
        xml.select("/b:books/book[@id='bk001']/@id").get().select("/anything");
    }

    @Test
    public void testSelectListOfElements() throws Exception {
        final List<XmlContext> contexts = xml.selectAll("/b:books/book");

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
        final Optional<XmlContext> optionalContext = xml.select("/b:books/book[@id='bk002']/@id");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("bk002", optionalContext.get().asString());
    }

    @Test
    public void testSelectElementValueToString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/book[@id='bk001']/genre/text()");

        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("Fiction", optionalContext.get().asString());
    }

    @Test
    public void testElementToUnformattedString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/book[2]");
        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("<book id=\"bk002\"><author>Poet</author><title>The Poet's First Poem</title><genre>Poem</genre><price>24.95</price><review>Least poetic poems.</review></book>",
                optionalContext.get().asString()
        );
    }

    @Test
    public void testElementToFormattedString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/book[1]");
        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("<book id=\"bk001\">\n" +
                        "\t<author>Writer</author>\n" +
                        "\t<title>The First Book</title>\n" +
                        "\t<genre>Fiction</genre>\n" +
                        "\t<price>44.95</price>\n" +
                        "\t<pub_date>2000-10-01</pub_date>\n" +
                        "\t<review>An amazing story of nothing.</review>\n" +
                        "</book>",
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
        final List<XmlContext> contexts = xml.selectAll("//author | //title");
        assertFalse(contexts.isEmpty());
        assertEquals("<author>Writer</author>",
                contexts.get(0).asString(true));
        assertEquals("<author>Poet</author>",
                contexts.get(1).asString(true));
        assertEquals("<title>The First Book</title>",
                contexts.get(2).asString(true));
        assertEquals("<title>The Poet's First Poem</title>",
                contexts.get(3).asString(true));

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

        assertFalse(xml.select("//book[@id='bk003']").isPresent());
        bw = new BufferedWriter(new FileWriter(file));
        bw.write("<?xml version=\"1.0\"?>\n" +
                "<b:books xmlns:b=\"urn:books:unqualified\">\n" +
                "    <book id=\"bk001\">\n" +
                "        <author>Writer</author>\n" +
                "        <title>The First Book</title>\n" +
                "        <genre>Fiction</genre>\n" +
                "        <price>44.95</price>\n" +
                "        <pub_date>2000-10-01</pub_date>\n" +
                "        <review>An amazing story of nothing.</review>\n" +
                "    </book>\n" +
                "\n" +
                "    <book id=\"bk002\">\n" +
                "        <author>Poet</author>\n" +
                "        <title>The Poet's First Poem</title>\n" +
                "        <genre>Poem</genre>\n" +
                "        <price>24.95</price>\n" +
                "        <review>Least poetic poems.</review>\n" +
                "    </book>\n" +
                "\n" +
                "    <book id=\"bk003\">\n" +
                "        <author>Edgar Allan Poe</author>\n" +
                "        <title>HELLO</title>\n" +
                "        <genre>Sci-Fi</genre>\n" +
                "        <price>15.95</price>\n" +
                "        <review>VERY GOOD 5/7</review>\n" +
                "    </book>\n" +
                "</b:books>");
        bw.flush();
        bw.close();
        long currentTime = System.currentTimeMillis();
        //wait for AutoUpdater to actually get changed file date from os.
        boolean present = false;
        while(System.currentTimeMillis() - currentTime <= 1000000 && !present ) {
            //Try check again in 500 ms
            Thread.sleep(3000);
            present = xml.select("//book[@id='bk003']").isPresent();
        }
        Optional<XmlContext> hello = xml.select("//book[@id='bk003']");
        if (hello.isPresent()) {
            assertEquals("<book id=\"bk003\">\n" +
                            "\t<author>Edgar Allan Poe</author>\n" +
                            "\t<title>HELLO</title>\n" +
                            "\t<genre>Sci-Fi</genre>\n" +
                            "\t<price>15.95</price>\n" +
                            "\t<review>VERY GOOD 5/7</review>\n" +
                            "</book>",
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

