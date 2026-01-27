/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
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

    private final Thread watchThread = new Thread(this::watchEvents);
    private final File watchedDir;
    private final WatchService watchService;
    private final WatchKey watchKey;
    private final List<PathWatchEventListener> listeners = new ArrayList<>();
    private boolean watching;

    public DirectoryWatchdog(File directory) {
        if (directory == null) {
            throw new IllegalArgumentException("Watched path is NULL");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Watched path %s is not a directory".formatted(directory.getAbsolutePath()));
        }
        if (!directory.exists()) {
            throw new IllegalArgumentException("Watched directory %s does not exist".formatted(directory.getAbsolutePath()));
        }
        watchedDir = directory;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            watchKey = watchedDir.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    public void addEventListener(PathWatchEventListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(PathWatchEventListener listener) {
        listeners.remove(listener);
    }

    public void startWatching() {
        if (watchThread.isAlive()) return;
        watching = true;
        watchThread.setDaemon(true);
        watchThread.start();
        Logger.info("Start watching directory {}", watchedDir);
    }

    public void dispose() {
        watching = false;
        watchKey.cancel();
        watchThread.interrupt();
        try {
            watchThread.join(50);
            watchService.close();
            listeners.clear();
            Logger.info("Stopped watching directory {}", watchedDir);
        } catch (Exception x) {
            Logger.warn("Exception occurred when stopping directory watchdog");
        }
    }

    @SuppressWarnings("unchecked")
    private void watchEvents() {
        while (watching && !Thread.currentThread().isInterrupted()) {
            try {
                // Blocks until at least one event is available or watch key is invalid
                final WatchKey key = watchService.take();

                if (!key.isValid()) {
                    Logger.warn("WatchKey became invalid for {}", watchedDir);
                    break;
                }

                final List<WatchEvent<?>> watchEvents = key.pollEvents();
                if (!watchEvents.isEmpty()) {
                    final List<WatchEvent<Path>> eventList = watchEvents.stream()
                        .map(e -> (WatchEvent<Path>) e)
                        .filter(event -> {
                            final Path relPath = event.context(); // file or directory name in watched directory
                            return watchedDir.toPath().resolve(relPath).toFile().isFile(); // only real files
                        })
                        .toList();
                    if (!eventList.isEmpty()) {
                        try {
                            listeners.forEach(listener -> listener.handleWatchEvents(eventList));
                        }
                        catch (Exception x) {
                            Logger.error("Exception occurred while handling watch events");
                        }
                    }
                }

                // Very important: reset the key so we receive future events!
                final boolean valid = key.reset();
                if (!valid) {
                    Logger.warn("WatchKey reset failed for {}", watchedDir);
                    break;
                }

            } catch (InterruptedException e) {
                Logger.debug("Directory watchdog interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                Logger.debug("WatchService closed");
                break;
            }
        }
    }
}