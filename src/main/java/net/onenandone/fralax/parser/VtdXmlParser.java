package net.onenandone.fralax.parser;

import com.ximpleware.ParseException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.onenandone.fralax.FralaxException;
import net.onenandone.fralax.XmlParser;

import java.io.IOException;


/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class VtdXmlParser implements XmlParser {

    @Override
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
