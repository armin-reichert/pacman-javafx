/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class DirectoryWatchdog {

    private final Thread pollingThread = new Thread(this::pollEvents);
    private final File watchedDir;
    private final WatchService watchService;
    private final WatchKey watchKey;
    private final List<PathWatchEventListener> eventListeners = new ArrayList<>();
    private boolean polling;

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
            watchService = FileSystems.getDefault().newWatchService();
            watchKey = watchedDir.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    public void addEventListener(PathWatchEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void removeEventListener(PathWatchEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    public void startWatching() {
        if (pollingThread.isAlive()) return;
        polling = true;
        pollingThread.setDaemon(true);
        pollingThread.start();
        Logger.info("Start watching directory {}", watchedDir);
    }

    public void dispose() {
        polling = false;
        watchKey.cancel();
        pollingThread.interrupt();
        try {
            pollingThread.join(100);
            watchService.close();
            eventListeners.clear();
            Logger.info("Stopped watching directory {}", watchedDir);
        } catch (Exception x) {
            Logger.error("Exception occurred when stopping directory watchdog");
        }
    }

    public boolean isWatching() { return polling && pollingThread.isAlive(); }

    @SuppressWarnings("unchecked")
    private void pollEvents() {
        while (polling) {
            final List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
            if (!watchEvents.isEmpty()) {
                final List<WatchEvent<Path>> pathEvents = watchEvents.stream().map(e -> (WatchEvent<Path>) e).toList();
                eventListeners.forEach(listener -> listener.handlePathEvents(pathEvents));
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Logger.debug("Directory polling thread interrupted");
                Thread.currentThread().interrupt(); // restore flag
            }
        }
    }
}