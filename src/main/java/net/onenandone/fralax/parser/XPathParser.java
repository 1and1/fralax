package net.onenandone.fralax.parser;

import net.onenandone.fralax.model.XPathResult;

import javax.xml.xpath.XPathException;
import java.util.Optional;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 05.04.16.
 * @version 1.0
 */
public interface XPathParser {

    Optional<XPathResult> searchFor(String xPathQuery) throws XPathException;
}
