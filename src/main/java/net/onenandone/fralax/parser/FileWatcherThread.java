package net.onenandone.fralax.parser;

import net.onenandone.fralax.FralaxException;
import net.onenandone.fralax.XmlContext;

import java.io.File;

/**
 * A FileWatcher that watches for any changes in the specified document and notifies the parser if a change
 * occurs. Also see {@link XmlContext#isValid()} for more information on why this is necessary for the VtdParser.
 */
@SuppressWarnings("WeakerAccess")
public class FileWatcherThread extends Thread {

    private final String file;
    private long firstModified;
    private final VtdXmlParser parser;

    public FileWatcherThread(final String file, final VtdXmlParser parser) {
        this.file = file;
        this.parser = parser;
        firstModified = System.currentTimeMillis();
    }

    /**
     * The run function for the File Watcher Thread. Creates a WatchService and polls events happening in the directory
     * of the file specified in the Constructor. When an external change occurs, it notifies the parser by setting its validity field to false.
     */
    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            if (firstModified < new File(file).lastModified()) {
                parser.setValid(false);
                return;
            }
            try {
                //reduce cpu time
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new FralaxException("Error in the FileWatcher for file " + file);
            }
        }
    }

}
