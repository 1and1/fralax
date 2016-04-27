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
        xml.registerNamespace("b", "urn:books:qualified");
    }

    @Test
    public void setNoSelectToString() throws Exception {
        assertEquals(
                "<x:books xmlns:x=\"urn:books:qualified\">\n" +
                        "\t<x:book id=\"bk001\">\n" +
                        "\t\t<x:author>Writer</x:author>\n" +
                        "\t\t<x:title>The First Book</x:title>\n" +
                        "\t\t<x:genre>Fiction</x:genre>\n" +
                        "\t\t<x:price>44.95</x:price>\n" +
                        "\t\t<x:pub_date>2000-10-01</x:pub_date>\n" +
                        "\t\t<x:review>An amazing story of nothing.</x:review>\n" +
                        "\t</x:book>\n" +
                        "\t<x:book id=\"bk002\">\n" +
                        "\t\t<x:author>Poet</x:author>\n" +
                        "\t\t<x:title>The Poet's First Poem</x:title>\n" +
                        "\t\t<x:genre>Poem</x:genre>\n" +
                        "\t\t<x:price>24.95</x:price>\n" +
                        "\t\t<x:review>Least poetic poems.</x:review>\n" +
                        "\t</x:book>\n" +
                        "</x:books>",
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
        assertEquals("<x:book id=\"bk002\"><x:author>Poet</x:author><x:title>The Poet's First Poem</x:title><x:genre>Poem</x:genre><x:price>24.95</x:price><x:review>Least poetic poems.</x:review></x:book>",
                optionalContext.get().asString()
        );
    }

    @Test
    public void testElementToFormattedString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/b:book[1]");
        assertNotNull(optionalContext);
        assertTrue(optionalContext.isPresent());
        assertEquals("<x:book id=\"bk001\">\n" +
                        "\t<x:author>Writer</x:author>\n" +
                        "\t<x:title>The First Book</x:title>\n" +
                        "\t<x:genre>Fiction</x:genre>\n" +
                        "\t<x:price>44.95</x:price>\n" +
                        "\t<x:pub_date>2000-10-01</x:pub_date>\n" +
                        "\t<x:review>An amazing story of nothing.</x:review>\n" +
                        "</x:book>",
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
        assertEquals("<x:author>Writer</x:author>",
                contexts.get(0).asString(true));
        assertEquals("<x:author>Poet</x:author>",
                contexts.get(1).asString(true));
        assertEquals("<x:title>The First Book</x:title>",
                contexts.get(2).asString(true));
        assertEquals("<x:title>The Poet's First Poem</x:title>",
                contexts.get(3).asString(true));

    }


}

