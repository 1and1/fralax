package net.onenandone.fralax.parser;

import com.ximpleware.ParseException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.onenandone.fralax.FralaxException;
import net.onenandone.fralax.XmlParser;

import java.io.IOException;


/**
 * A VtdXmlParser based on XPath and using <a href="http://vtd-xml.sourceforge.net/">VTD-XML</a> as the underlying parser.
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class VtdXmlParser implements XmlParser {

    @Override
    public VtdXmlParserRootContext parse(final String file, boolean autoUpdate) {
        try {
            return new VtdXmlParserRootContext(file, autoUpdate);
        } catch (final IOException e) {
            throw new FralaxException("Error in reading Input File " + file, e);
        } catch (final ParseException e) {
            throw new FralaxException("Error in Parsing the Input File " + file + " with parser " + VtdXmlParser.class.toString(), e);
        }
    }
}
