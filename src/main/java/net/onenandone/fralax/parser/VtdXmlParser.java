package net.onenandone.fralax.parser;

import com.ximpleware.ParseException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.onenandone.fralax.FralaxException;
import net.onenandone.fralax.XmlParser;

import java.io.IOException;


/**
 * @author Daniel Draper Johann Bähler
 *         Created on 01.04.16.
 * @version 1.0
 *          A VtdXmlParser based on XPath and using <a href="http://vtd-xml.sourceforge.net/">VTD-XML</a> as the underlying parser.
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class VtdXmlParser implements XmlParser {

    @Override
    /**
     * @see XmlParser#parse(String)
     */
    public VtdXmlParserContext parse(final String file) {
        try {
            return new VtdXmlParserContext(file);
        } catch (final IOException e) {
            throw new FralaxException("Error in reading Input File " + file, e);
        } catch (final ParseException e) {
            throw new FralaxException("Error in Parsing the Input File " + file + " with parser " + VtdXmlParser.class.toString(), e);
        }
    }
}
