package net.onenandone.fralax;

import javax.xml.xpath.XPathException;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 06.04.16.
 * @version 1.0
 */
public class WrongXPathForTypeException extends XPathException {
    /**
     * <p>Constructs a new <code>XPathException</code>
     * with the specified detail <code>message</code>.</p>
     * <p>
     * <p>The <code>cause</code> is not initialized.</p>
     * <p>
     * <p>If <code>message</code> is <code>null</code>,
     * then a <code>NullPointerException</code> is thrown.</p>
     *
     * @param message The detail message.
     * @throws NullPointerException When <code>message</code> is
     *                              <code>null</code>.
     */
    public WrongXPathForTypeException(String message) {
        super(message);
    }
}
