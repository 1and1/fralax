package net.onenandone.fralax.parser;

import com.ximpleware.ParseException;
import net.onenandone.fralax.example.VtdXMLParserImpl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 */
public class XMLParserDefaultFactory implements XMLParserFactory {

    /**
     * @see XMLParserFactory#createNewInstance(String, Map)
     */
    @Override
    public XMLParser createNewInstance(String file, Map<String, String> namespaces) {
        try {
            return new VtdXMLParserImpl(new File(file), namespaces);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
