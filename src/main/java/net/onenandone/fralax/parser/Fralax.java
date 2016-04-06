package net.onenandone.fralax.parser;

import com.ximpleware.ParseException;
import net.onenandone.fralax.example.VtdXMLParserImpl;
import net.onenandone.fralax.model.Context;

import java.io.IOException;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 06.04.16.
 * @version 1.0
 * Factory used to create the default (Vtd) XMLParser or a specific parser.
 * REQUIRES Parser Implementation to have default constructor!
 */
public class Fralax {


    public static Context parse (final String file) throws IOException, ParseException, InstantiationException, IllegalAccessException {
        return parse(file, VtdXMLParserImpl.class);
    }

    public static <T extends Context> T parse(final String file, final Class<? extends XPathParser<T>> xmlParserClass) throws IOException, ParseException, IllegalAccessException, InstantiationException {
        XPathParser<T> xPathParser = xmlParserClass.newInstance();
        return xPathParser.parse(file);
    }


}
