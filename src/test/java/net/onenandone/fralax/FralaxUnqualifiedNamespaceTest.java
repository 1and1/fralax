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
        xml.registerNamespace("b", "urn:books:unqualified");
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
                        "  <author>Writer</author>\n" +
                        "  <title>The First Book</title>\n" +
                        "  <genre>Fiction</genre>\n" +
                        "  <price>44.95</price>\n" +
                        "  <pub_date>2000-10-01</pub_date>\n" +
                        "  <review>An amazing story of nothing.</review>\n" +
                        "</book>\n",
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
    public void testSelectMultiple() throws Exception {
        final List<XmlContext> contexts = xml.selectAll("//author | //title");
        assertFalse(contexts.isEmpty());
        assertEquals("<author>Writer</author>\n",
                contexts.get(0).asString(true));
        assertEquals("<author>Poet</author>\n",
                contexts.get(1).asString(true));
        assertEquals("<title>The First Book</title>\n",
                contexts.get(2).asString(true));
        assertEquals("<title>The Poet's First Poem</title>\n",
                contexts.get(3).asString(true));

    }

    @Test
    public void testWatcherService() throws Exception {
        for (int i = 0; i < 50; i++) {
            testWatcherServiceOnce();
        }
    }

    private void testWatcherServiceOnce() throws Exception {
        setUp();
        assertTrue(xml.isValid());
        //We wait as to make sure the system calls for the lastModified date are actually changed. In production,
        //this just means, that a change is not registered immediately as we have to wait for the OS to write
        //last modified date and our thread has to realize the check.
        Thread.sleep(1000);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(xml.asString(true));
        bw.flush();
        bw.close();
        Thread.sleep(500);
        assertFalse(xml.isValid());
    }


}

