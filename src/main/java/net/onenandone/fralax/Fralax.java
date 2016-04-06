package net.onenandone.fralax;

import com.ximpleware.ParseException;
import net.onenandone.fralax.parser.VtdXmlParser;

import java.io.IOException;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 06.04.16.
 * @version 1.0
 * Factory used to create the default (Vtd) XMLParser or a specific parser.
 * REQUIRES Parser Implementation to have default constructor!
 */
public class Fralax {


    public static XmlContext parse (final String file) throws IOException, ParseException, InstantiationException, IllegalAccessException {
        return parse(file, VtdXmlParser.class);
    }

    public static <T extends XmlContext> T parse(final String file, final Class<? extends XmlParser<T>> xmlParserClass) throws IOException, ParseException, IllegalAccessException, InstantiationException {
        XmlParser<T> xmlParser = xmlParserClass.newInstance();
        return xmlParser.parse(file);
    }


}
