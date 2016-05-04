package net.onenandone.fralax;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("WeakerAccess")
/**
 * A Managed XMLContext that ensures if the underlying xml file is changed externally, the internal representation is updated correctly.
 */
public class ManagedXmlContext implements  XmlContext {

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

    /**
     * As to stop auto-updating of the file to happen, one should use the xml context in an unmanaged fashion, if you expect
     * to use the root context for different xpath queries multiple times. This prevents the following behavior:
     * <pre>
     *     {@code
     *     ManagedXmlContext xml = Fralax.watch(FralaxTest.class.getResource("/driverVehicleInfo.xml").getFile(), VtdXmlParser.class);
     *     xml.selectAll("//driver");
     *     //... do something with result
     *     //...
     *     //Now the xml file is changed externally
     *     xml.selectAll("//driver"); //Requery same query
     *     //Result is different from first query.
     *     }
     * </pre>
     * Instead, when using the unmanaged function, e.g.
     * <pre>
     *     {@code
     *      ManagedXmlContext xml = Fralax.watch(FralaxTest.class.getResource("/driverVehicleInfo.xml").getFile(), VtdXmlParser.class);
     *      XMLContext unmanaged = xml.unmanaged();
     *      unmanaged.selectAll("//driver");
     *      //... do something with result
     *      //...
     *     //Now the xml file is changed externally
     *     xml.selectAll("//driver"); //Requery same query
     *     //Now the result is the same as for the original query.
     *     }
     * </pre>
     * @return an unmanaged version of this context.
     */
    public XmlContext unmanaged() {
        return rootContext;
    }
}
