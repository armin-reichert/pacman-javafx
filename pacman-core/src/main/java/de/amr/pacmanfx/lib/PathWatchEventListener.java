/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.List;

public interface PathWatchEventListener {
    void handlePathEvents(List<WatchEvent<Path>> events);
}
