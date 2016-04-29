package net.onenandone.fralax;

import com.ximpleware.ParseException;

import java.io.IOException;

/**
 * A root context is an xml context that was the first returned context after the parsing of a file. Includes optional functionality
 * to reparse the xml document either manually or automatically using the AutoUpdater thread. Methods all are required if the AutoUpdater is
 * to be used.
 */
public interface XmlRootContext extends XmlContext {


    /**
     * Stops the associated AutoUpdater from running if it was running.
     */
    default void stopAutoUpdate() {
    }

    /**
     * Starts a new AutoUpdater for this root Context.
     */
    default void startAutoUpdate() {
    }

    /**
     * An optional method that will reparse the root xml context. Called by the AutoUpdater if it's used.
     */
    default void update() throws IOException, ParseException {

    }


    /**
     * An optional method that will return if the underlying document has been modified since the first parse. Will return true if
     * the document was not modified and false if it was. In the case of in-place parsers like SAX or StAX, this method
     * is not necessarily needed, as continued parses on a file will re-read the entire document. However, it
     * is preferred to copy and save the original file and use this for continued parsing as to not cause the following
     * case:
     * <pre>
     * {@code
     * XmlContext xml = Fralax.parse(fileToParse);
     * xml.select("//element1");
     * //Now the underlying xml file is changed externally removing all content.
     * xml.select("//elementThatIsChildOfElement1"); //Will throw a FralaxException in in-place parsers as the xml file is now empty.
     * }
     * </pre>
     * In the case of in-memory parsers, (like the default VtdParser) this method should always be implemented as to give the user a chance to
     * check for changes in the original document and reparse the document if needed.
     *
     * @return true if the underlying xml file was modified externally false if not.
     */
    default boolean wasChanged() {
        return false;
    }

}
