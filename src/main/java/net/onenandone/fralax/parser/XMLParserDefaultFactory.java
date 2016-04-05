package net.onenandone.fralax.parser;

import com.ximpleware.ParseException;
import lombok.extern.java.Log;
import net.onenandone.fralax.example.VtdXMLParserImpl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 */
@Log
public class XMLParserDefaultFactory implements XMLParserFactory {

    /**
     * @see XMLParserFactory#createNewInstance(String, Map)
     */
    @Override
    public XMLParser createNewInstance(String file, Map<String, String> namespaces) {
        try {
            return new VtdXMLParserImpl(new File(file), namespaces);
        } catch (IOException e) {
            log.log(Level.SEVERE, "IOException when creating default VTD parser. Filename provided was " + file, e);
        } catch (ParseException e) {
            log.log(Level.SEVERE, "ParseException when parsing file " + file + "with default VTD Parser", e);
        }
        return null;
    }

    public static XMLParser createNewInstance(String file) {
        try {
            return new VtdXMLParserImpl(new File(file), new HashMap<>());
        } catch (IOException e) {
            log.log(Level.SEVERE, "IOException when creating default VTD parser. Filename provided was " + file, e);
        } catch (ParseException e) {
            log.log(Level.SEVERE, "ParseException when parsing file " + file + "with default VTD Parser", e);
        }
        return null;
    }

}
