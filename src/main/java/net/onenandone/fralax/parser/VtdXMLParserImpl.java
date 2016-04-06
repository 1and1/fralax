package net.onenandone.fralax.parser;

import com.ximpleware.ParseException;
import lombok.extern.slf4j.Slf4j;
import net.onenandone.fralax.XPathParser;

import java.io.IOException;


/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 */
@Slf4j
public class VtdXMLParserImpl implements XPathParser<VtdParserContext> {

    public VtdXMLParserImpl() {

    }

    @Override
    public VtdParserContext parse(String file) {
        try {
            return new VtdParserContext(file);
        } catch (IOException e) {
            log.error("Error in reading Input File " + file, e);
        } catch (ParseException e) {
            log.error("Error in Parsing the Input File " + file + " with parser " + VtdXMLParserImpl.class.toString(), e);
        }
        return null;
    }
}
