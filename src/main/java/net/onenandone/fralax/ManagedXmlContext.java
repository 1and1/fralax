package net.onenandone.fralax;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ManagedXmlContext implements XmlContext {

    private static final Object MUTEX = new Object();

    private final File file;
    private final Class<? extends XmlParser> xmlParserClass;

    private XmlContext rootContext;
    private long lastModification;

    ManagedXmlContext(final String file, final Class<? extends XmlParser> xmlParserClass) {
        Objects.requireNonNull(file, "the xml file may not be null");
        Objects.requireNonNull(xmlParserClass, "the xml parser class may not be null");

        this.file = new File(file);
        this.xmlParserClass = xmlParserClass;

        try {
            this.rootContext = xmlParserClass.newInstance().parse(this.file.getAbsolutePath());
            this.lastModification = this.file.lastModified();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new FralaxException("could not instantiate xml parser class '" + xmlParserClass.getCanonicalName() + "'", e);
        }
    }

    private synchronized void checkLastModification() {
        if (file.lastModified() > lastModification) {
            synchronized (MUTEX) {
                if (file.lastModified() > lastModification) {
                    try {
                        this.rootContext = xmlParserClass.newInstance().parse(file.getAbsolutePath());
                        this.lastModification = this.file.lastModified();
                    } catch (final InstantiationException | IllegalAccessException e) {
                        throw new FralaxException("could not instantiate xml parser class '" + xmlParserClass.getCanonicalName() + "'", e);
                    }
                }
            }
        }
    }

    @Override
    public Optional<XmlContext> select(final String xpath) throws FralaxException {
        checkLastModification();
        return this.rootContext.select(xpath);
    }

    @Override
    public List<XmlContext> selectAll(final String xpath) throws FralaxException {
        checkLastModification();
        return this.rootContext.selectAll(xpath);
    }

    @Override
    public String asString() {
        checkLastModification();
        return this.rootContext.asString();
    }

    @Override
    public String asString(final boolean formatted) {
        checkLastModification();
        return this.rootContext.asString(formatted);
    }

}
