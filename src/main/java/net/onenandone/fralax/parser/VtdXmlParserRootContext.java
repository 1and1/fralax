package net.onenandone.fralax.parser;

import com.ximpleware.ParseException;
import lombok.Synchronized;
import net.onenandone.fralax.AutoUpdater;
import net.onenandone.fralax.FralaxException;
import net.onenandone.fralax.XmlContext;
import net.onenandone.fralax.XmlRootContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 29.04.16.
 * @version 1.0
 */
@SuppressWarnings("WeakerAccess")
public class VtdXmlParserRootContext implements XmlRootContext {

    private VtdXmlParserContext context;
    private long firstLoaded;
    private final String file;
    private AutoUpdater autoUpdater;

    /**
     * Default constructor used to create a newly parsed XMLContext from a certain file.
     *
     * @param file the file to parse.
     * @throws IOException    thrown when an error occurs while opening the file.
     * @throws ParseException thrown when an error occurs during parsing of the xml file.
     */
    VtdXmlParserRootContext(final String file, final boolean autoUpdate) throws IOException, ParseException {
        this.context = new VtdXmlParserContext(file);
        this.file = file;
        this.firstLoaded = System.currentTimeMillis();
        if (autoUpdate) {
            autoUpdater = new AutoUpdater(this);
            autoUpdater.start();
        }
    }

    /**
     * Stops the associated AutoUpdater from running if it was running.
     */
    @Override
    @Synchronized
    public void stopAutoUpdate() {
        if (autoUpdater != null && autoUpdater.isAlive()) {
            autoUpdater.getAutoUpdate().lazySet(false);
            try {
                autoUpdater.join();
            } catch (InterruptedException e) {
                throw new FralaxException("Error when closing the AutoUpdate for file " + file);
            }
        }
    }

    @Override
    @Synchronized
    public void startAutoUpdate() {
        if (autoUpdater == null || !autoUpdater.isAlive()) {
            autoUpdater = new AutoUpdater(this);
            autoUpdater.start();
        }
    }

    @Override
    @Synchronized
    public void update() throws IOException, ParseException {
        context = new VtdXmlParserContext(file);
        firstLoaded = System.currentTimeMillis();
    }

    @Override
    @Synchronized
    public boolean wasChanged() {
        return firstLoaded < new File(file).lastModified();
    }

    @Override
    @Synchronized
    public Optional<XmlContext> select(String xpath) {
        return context.select(xpath);
    }

    @Override
    @Synchronized
    public List<XmlContext> selectAll(String xpath) {
        return context.selectAll(xpath);
    }

    @Override
    @Synchronized
    public String asString() {
        return context.asString();
    }
}
