package net.onenandone.fralax;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 *
 */
@RequiredArgsConstructor
public class FileWatcherThread extends Thread {

    private WatchService watchService;
    @Getter
    private boolean isValid = true;
    private final String file;

    @Override
    public void run() {
        try {
            String onlyPath = file.replaceFirst("/([^/.]*)\\.xml", "") + "/";
            if (!(onlyPath + new File(file).getName()).equals(file)) {
                throw new FralaxException("Invalid Filename provided, make sure file " + file + " is valid and an xml.");
            }
            final Path path = Paths.get(onlyPath);
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, ENTRY_MODIFY);
            while (true) {
                WatchKey watchKey;
                try {
                    watchKey = watchService.take();
                } catch (InterruptedException e) {
                    System.out.println("what");
                    throw new FralaxException("WatcherService was interrupted unexpectedly " + file, e);
                }
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    //we only register "ENTRY_MODIFY" so the context is always a Path.
                    final Path changed = (Path) event.context();
                    //System.out.println(changed.toFile().toString().equals(Paths.get(file).toFile()));
                    //TODO: Fix this Check to work...
                    System.out.println(changed.getName(0));
                    System.out.println(changed.endsWith(file.replaceAll("(.*)/", "")));
                    if (changed.endsWith(file.replaceAll("(.*)/", ""))) {
                        System.out.println("I AM CONFUSED");
                        isValid = false;
                    }
                }
                //reset the key
                watchKey.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
