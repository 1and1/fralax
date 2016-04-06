package net.onenandone.fralax.parser;

import com.ximpleware.ParseException;
import lombok.extern.slf4j.Slf4j;
import net.onenandone.fralax.XmlParser;

import java.io.IOException;


/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 */
@Slf4j
public class VtdXmlParser implements XmlParser<VtdXmlParserContext> {

    public VtdXmlParser() {

    }

    @Override
    public VtdXmlParserContext parse(String file) {
        try {
            return new VtdXmlParserContext(file);
        } catch (IOException e) {
            log.error("Error in reading Input File " + file, e);
        } catch (ParseException e) {
            log.error("Error in Parsing the Input File " + file + " with parser " + VtdXmlParser.class.toString(), e);
        }
        return null;
    }
}
