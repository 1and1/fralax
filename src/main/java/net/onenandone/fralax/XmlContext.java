package net.onenandone.fralax;

import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 06.04.16.
 * @version 1.0
 */
public interface XmlContext {
    void registerNamespace(final String key, final String value);
    Optional<XmlContext> select(final String xpath) throws WrongXPathForTypeException;
    List<XmlContext> selectAll(final String xpath) throws WrongXPathForTypeException;
    String asString();
    String asFormattedString();
}
