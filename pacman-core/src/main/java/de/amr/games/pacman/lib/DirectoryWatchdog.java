/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
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

public class DirectoryWatchdog {

    private final File watchedDir;
    private final WatchKey watchKey;
    private Consumer<List<WatchEvent<?>>> eventConsumer;

    public DirectoryWatchdog(File path) {
        if (path == null) {
            throw new IllegalArgumentException("Watched path is NULL");
        }
        if (!path.isDirectory()) {
            throw new IllegalArgumentException("Watched path %s is not a directory".formatted(path.getAbsolutePath()));
        }
        if (!path.exists()) {
            throw new IllegalArgumentException("Watched directory %s does not exist".formatted(path.getAbsolutePath()));
        }
        watchedDir = path;
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            watchKey = watchedDir.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            setEventConsumer(eventList -> {
                for (var event : eventList) {
                    Logger.info(event);
                }
            });
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    public void setEventConsumer(Consumer<List<WatchEvent<?>>> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    public void startWatching() {
        Thread pollingThread = new Thread(this::pollingLoop);
        pollingThread.setDaemon(true);
        pollingThread.start();
        Logger.info("Start watching directory {}", watchedDir);
    }

    private void pollingLoop() {
        for (;;) {
            var polledEvents = watchKey.pollEvents();
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