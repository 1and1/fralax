package net.onenandone.fralax.parser;

import net.onenandone.fralax.model.Context;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 05.04.16.
 * @version 1.0
 */
public interface XPathParser<T extends Context> {

    T parse(final String file);

}
