package net.onenandone.fralax;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class FralaxQualifiedNamespaceTest {

    private XmlContext xml;

    @Before
    public void setUp() throws Exception {
        xml = Fralax.parse(FralaxTest.class.getResource("/books-qualified.xml").getFile());
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


}

