package net.onenandone.fralax;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 05.04.16.
 * @version 1.0
 */
public interface XmlParser<T extends XmlContext> {

    T parse(final String file);

}
