package de.amr.games.pacman.lib;

import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class CustomMapWatchdog {

    private final File watchedDirectory;
    private final WatchKey customMapDirWatcher;
    private Consumer<List<WatchEvent<?>>> eventConsumer;

    public CustomMapWatchdog(File watchedDirectory) throws IOException {
        if (watchedDirectory == null) {
            throw new IllegalArgumentException();
        }
        if (!watchedDirectory.isDirectory() || !watchedDirectory.exists()) {
            throw new IllegalArgumentException();
        }
        this.watchedDirectory = watchedDirectory;
        WatchService watchService = FileSystems.getDefault().newWatchService();
        customMapDirWatcher = watchedDirectory.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        setEventConsumer(eventList -> {
            for (var event : eventList) {
                Logger.info(event);
            }
        });
    }

    public void setEventConsumer(Consumer<List<WatchEvent<?>>> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    public void startWatching() {
        Thread pollingThread = new Thread(this::pollingLoop);
        pollingThread.setDaemon(true);
        pollingThread.start();
        Logger.info("Watching {}", watchedDirectory);
    }

    private void pollingLoop() {
        for (;;) {
            var polledEvents = customMapDirWatcher.pollEvents();
            if (!polledEvents.isEmpty()) {
                eventConsumer.accept(polledEvents);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Logger.error("Interrupted");
            }
        }
    }
}
