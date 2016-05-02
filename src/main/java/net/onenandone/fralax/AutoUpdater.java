package net.onenandone.fralax;

import com.ximpleware.ParseException;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Autoupdater is a Thread that will automatically call the ({@link XmlRootContext#update()}) function
 * in case the underlying xml file is changed.
 */
public class AutoUpdater extends Thread {

    @Getter
    @Setter
    private AtomicBoolean autoUpdate;
    private XmlRootContext toUpdate;

    public AutoUpdater(XmlRootContext toUpdate) {
        this.autoUpdate = new AtomicBoolean(true);
        this.toUpdate = toUpdate;
    }

    @Override
    public void run() {
        while (autoUpdate.get()) {
            if (toUpdate.wasChanged()) {
                try {
                    toUpdate.update();
                } catch (IOException | ParseException e) {
                    throw new FralaxException("Error while parsing the updated File, maybe malformed", e);
                }
            }
            //to not clog CPU
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new FralaxException("Error in the AutoUpdater", e);
            }
        }
    }

}
