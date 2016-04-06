package net.onenandone.fralax.parser;

import net.onenandone.fralax.XmlContext;
import net.onenandone.fralax.FralaxException;

import java.util.List;
import java.util.Optional;

/**
 * @author Johann Bähler
 * @version 1.0
 *          A XMLContext representing a single value in an XML document.
 *          E.g what is returned after searching for a single attribute value ("/driver/@id")
 *          Does not support further searching using xpath queries.
 */
class ValueContext implements XmlContext {

    private final String value;

    /**
     * Creates the ValueContext.
     *
     * @param value value to assign to the context.
     */
    ValueContext(final String value) {
        this.value = value;
    }

    @Override
    /**
     * Not Supported.
     */
    public void registerNamespace(String key, String value) {
        throw new UnsupportedOperationException("cannot register namespace for value");
    }

    @Override
    /**
     * Not Supported.
     */
    public Optional<XmlContext> select(String xpath) throws FralaxException {
        throw new UnsupportedOperationException("cannot select elements within value context");
    }

    @Override
    /**
     * Not Supported.
     */
    public List<XmlContext> selectAll(String xpath) throws FralaxException {
        throw new UnsupportedOperationException("cannot select elements within value context");
    }

    @Override
    /**
     * @see XmlContext#asString()
     */
    public String asString() {
        return value;
    }

    @Override
    /**
     * @see XmlContext#asFormattedString()
     */
    public String asFormattedString() {
        return value;
    }
}
