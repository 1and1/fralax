package net.onenandone.fralax;

import java.io.File;
import java.util.Objects;

/**
 * A Managed XML Parser autoupdates the root context whenever you reacquire it.
 */
@SuppressWarnings("WeakerAccess")
public class ManagedXmlParser {

    private static final Object MUTEX = new Object();

    private final File file;
    private final Class<? extends XmlParser> xmlParserClass;

    private XmlContext rootContext;
    private long lastModification;

    ManagedXmlParser(final String file, final Class<? extends XmlParser> xmlParserClass) {
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

    /**
     * Returns a current version of the root xml context for the file.
     * @return the (updated) root context.
     */
    public XmlContext getRootContext() {
        checkLastModification();
        return rootContext;
    }
}
