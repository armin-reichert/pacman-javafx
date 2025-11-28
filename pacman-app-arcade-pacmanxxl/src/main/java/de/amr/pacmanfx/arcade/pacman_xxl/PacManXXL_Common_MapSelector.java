/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.MapSelector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.CONFIG_KEY_COLOR_MAP;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class PacManXXL_Common_MapSelector implements MapSelector {

    public static final List<Map<String, String>> MAP_COLOR_SCHEMES = List.of(
            Map.of("fill", "#359c9c", "stroke", "#85e2ff", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#c2b853", "stroke", "#ffeace", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#86669c", "stroke", "#f6c4e0", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#ed0a04", "stroke", "#f0b4cd", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#2067c1", "stroke", "#65e5bb", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#c55994", "stroke", "#f760c0", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#12bc76", "stroke", "#ade672", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#5036d9", "stroke", "#5f8bcf", "door", "#fcb5ff", "pellet", "#feb8ae")
    );

    private static void addSampleCustomMapPrototypes(File dir) {
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
                URL url = PacManXXL_Common_MapSelector.class.getResource(path);
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

    private final File customMapDir;
    private final ObservableList<WorldMap> customMapPrototypes = FXCollections.observableArrayList();
    private final List<WorldMap> builtinMapPrototypes = new ArrayList<>();
    private MapSelectionMode selectionMode;

    public PacManXXL_Common_MapSelector(File dir) {
        customMapDir = requireNonNull(dir);
        selectionMode = MapSelectionMode.CUSTOM_MAPS_FIRST;
        addSampleCustomMapPrototypes(dir);
    }

    public MapSelectionMode selectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(MapSelectionMode mode) {
        selectionMode = requireNonNull(mode);
    }

    @Override
    public List<WorldMap> builtinMapPrototypes() {
        return builtinMapPrototypes;
    }

    @Override
    public ObservableList<WorldMap> customMapPrototypes() {
        return customMapPrototypes;
    }

    @Override
    public void loadCustomMapPrototypes() {
        File[] mapFiles = customMapDir.listFiles((dir, name) -> name.endsWith(".world"));
        if (mapFiles == null) {
            Logger.error("An error occurred accessing custom map directory {}", customMapDir);
            return;
        }
        if (mapFiles.length == 0) {
            Logger.info("No custom maps found");
        } else {
            Logger.info("{} custom map(s) found", mapFiles.length);
        }
        if (!customMapPrototypes.isEmpty()) {
            Logger.info("Custom maps have already been loaded");
            return;
        }
        for (File file : mapFiles) {
            try {
                WorldMap worldMap = WorldMap.loadFromFile(file);
                customMapPrototypes.add(worldMap);
                Logger.info("Custom map loaded from file {}", file);
            } catch (IOException x) {
                Logger.error(x);
                Logger.error("Could not read custom map from file {}", file);
            }
        }
    }

    @Override
    public void loadAllMapPrototypes() {
        if (builtinMapPrototypes.isEmpty()) {
            List<WorldMap> maps = MapSelector.loadMapsFromModule(getClass(), "maps/masonic_%d.world", 8);
            builtinMapPrototypes.addAll(maps);
        }
        loadCustomMapPrototypes();
    }

    @Override
    public WorldMap selectWorldMap(int levelNumber, Object... args) {
        WorldMap prototype = switch (selectionMode) {
            case NO_CUSTOM_MAPS -> {
                // first pick built-in maps in order, then randomly
                int index = levelNumber <= builtinMapPrototypes.size() ? levelNumber - 1: randomInt(0, builtinMapPrototypes.size());
                yield builtinMapPrototypes.get(index);
            }
            case CUSTOM_MAPS_FIRST -> {
                if (levelNumber <= customMapPrototypes.size()) {
                    // pick custom maps in order
                    yield customMapPrototypes.get(levelNumber - 1);
                }
                else {
                    // pick random built-in map
                    yield builtinMapPrototypes.get(randomInt(0, builtinMapPrototypes().size()));
                }
            }
            case ALL_RANDOM -> {
                int index = randomInt(0, customMapPrototypes().size() + builtinMapPrototypes().size());
                yield index < customMapPrototypes().size() ? customMapPrototypes.get(index) : builtinMapPrototypes.get(index - customMapPrototypes().size());
            }
        };

        WorldMap worldMap = new WorldMap(prototype);
        // if selected map is a built-in map, use a random color scheme to make it not so boring
        Map<String, String> colorScheme = builtinMapPrototypes.contains(prototype)
            ? MAP_COLOR_SCHEMES.get(randomInt(0, MAP_COLOR_SCHEMES.size()))
            : MapSelector.extractColorMap(prototype);
        worldMap.setConfigValue(CONFIG_KEY_COLOR_MAP, colorScheme);
        Logger.info("Map selected (Mode {}): {}", selectionMode, worldMap.url());

        return worldMap;
    }
}