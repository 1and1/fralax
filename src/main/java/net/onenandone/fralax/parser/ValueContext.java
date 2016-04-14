package net.onenandone.fralax.parser;

import net.onenandone.fralax.XmlContext;
import net.onenandone.fralax.FralaxException;

import java.util.List;
import java.util.Optional;

/**
 * A XMLContext representing a single value in an XML document.
 * E.g what is returned after searching for a single attribute value ("/driver/@id")
 * Does not support further searching using xpath queries.
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

    /** Not supported. */
    @Override
    public void registerNamespace(String key, String value) {
        throw new UnsupportedOperationException("cannot register namespace for value");
    }

    /** Not supported. */
    @Override
    public Optional<XmlContext> select(String xpath) throws FralaxException {
        throw new UnsupportedOperationException("cannot select elements within value context");
    }

    /** Not supported. */
    @Override
    public List<XmlContext> selectAll(String xpath) throws FralaxException {
        throw new UnsupportedOperationException("cannot select elements within value context");
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public String asFormattedString() {
        return value;
    }
}
