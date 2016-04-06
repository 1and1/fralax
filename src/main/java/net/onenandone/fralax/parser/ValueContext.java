package net.onenandone.fralax.parser;

import net.onenandone.fralax.Context;
import net.onenandone.fralax.WrongXPathForTypeException;

import java.util.List;
import java.util.Optional;

class ValueContext implements Context {

    private final String value;

    public ValueContext(final String value) {
        this.value = value;
    }

    @Override
    public void registerNamespace(String key, String value) {
        throw new UnsupportedOperationException("cannot register namespace for value");
    }

    @Override
    public Optional<Context> select(String xpath) throws WrongXPathForTypeException {
        throw new UnsupportedOperationException("cannot select elements within value context");
    }

    @Override
    public List<Context> selectAll(String xpath) throws WrongXPathForTypeException {
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
