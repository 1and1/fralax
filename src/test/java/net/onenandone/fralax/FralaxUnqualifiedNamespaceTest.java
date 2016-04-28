package net.onenandone.fralax;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class FralaxUnqualifiedNamespaceTest {

    private XmlContext xml;


    @Before
    public void setUp() throws Exception {
        xml = Fralax.parse(FralaxTest.class.getResource("/books-unqualified.xml").getFile());
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

}

