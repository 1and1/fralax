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
    public void setNoSelectToString() throws  Exception {
        assertEquals(
                "<x:books xmlns:x=\"urn:books:qualified\">\n" +
                "    <x:book id=\"bk001\">\n" +
                "        <x:author>Writer</x:author>\n" +
                "        <x:title>The First Book</x:title>\n" +
                "        <x:genre>Fiction</x:genre>\n" +
                "        <x:price>44.95</x:price>\n" +
                "        <x:pub_date>2000-10-01</x:pub_date>\n" +
                "        <x:review>An amazing story of nothing.</x:review>\n" +
                "    </x:book>\n" +
                "\n" +
                "    <x:book id=\"bk002\">\n" +
                "        <x:author>Poet</x:author>\n" +
                "        <x:title>The Poet's First Poem</x:title>\n" +
                "        <x:genre>Poem</x:genre>\n" +
                "        <x:price>24.95</x:price>\n" +
                "        <x:review>Least poetic poems.</x:review>\n" +
                "    </x:book>\n" +
                "</x:books>\n",
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
        assertEquals("<book id=\"bk002\"><author>Poet</author><title>The Poet's First Poem</title><genre>Poem</genre><price>24.95</price><review>Least poetic poems.</review></book>",
                optionalContext.get().asString()
        );
    }

    @Test
    public void testElementToFormattedString() throws Exception {
        final Optional<XmlContext> optionalContext = xml.select("/b:books/b:book[1]");
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

}

