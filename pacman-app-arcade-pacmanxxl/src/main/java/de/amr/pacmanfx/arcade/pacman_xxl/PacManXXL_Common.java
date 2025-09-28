/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.lib.worldmap.WorldMap;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public interface PacManXXL_Common {

    List<Map<String, String>> MAP_COLOR_SCHEMES = List.of(
        Map.of("fill", "#359c9c", "stroke", "#85e2ff", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#c2b853", "stroke", "#ffeace", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#86669c", "stroke", "#f6c4e0", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#ed0a04", "stroke", "#f0b4cd", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#2067c1", "stroke", "#65e5bb", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#c55994", "stroke", "#f760c0", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#12bc76", "stroke", "#ade672", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#5036d9", "stroke", "#5f8bcf", "door", "#fcb5ff", "pellet", "#feb8ae")
    );

    static void addSampleCustomMapPrototypes(File dir) {
        requireNonNull(dir);
        File[] files = dir.listFiles();
        if (files == null) {
            Logger.error("Could not access directory {}", dir);
            return;
        }
        if (files.length == 0) {
            Logger.info("Custom map directory is empty, fill with Junior Pac-Man maps...");
            for (int i = 1; i <= 15; ++i) {
                String mapName = "Jr. Pac-Man %02d.world".formatted(i);
                String path = "/de/amr/pacmanfx/arcade/pacman_xxl/maps/junior_pacman/" + mapName;
                URL url = PacManXXL_Common.class.getResource(path);
                if (url != null) {
                    try {
                        WorldMap worldMap = WorldMap.loadFromURL(url);
                        worldMap.saveToFile(new File(dir, mapName));
                    } catch (IOException e) {
                        Logger.error("Could not load map from {}", path);
                    }
                }
            }
        }
    }
}
