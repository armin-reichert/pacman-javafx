/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.CONFIG_KEY_COLOR_MAP;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class PacManXXL_Common_MapSelector implements MapSelector {

    private final File customMapDir;
    private final ObservableList<WorldMap> customMapPrototypes = FXCollections.observableArrayList();
    private final List<WorldMap> builtinMapPrototypes = new ArrayList<>();
    private MapSelectionMode selectionMode;

    public PacManXXL_Common_MapSelector(File dir) {
        customMapDir = requireNonNull(dir);
        selectionMode = MapSelectionMode.CUSTOM_MAPS_FIRST;
        PacManXXL_Common.addSampleCustomMapPrototypes(dir);
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
        customMapPrototypes.clear();
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
    public WorldMap provideWorldMap(int levelNumber, Object... args) {
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
            ? PacManXXL_Common.MAP_COLOR_SCHEMES.get(randomInt(0, PacManXXL_Common.MAP_COLOR_SCHEMES.size()))
            : MapSelector.extractColorMap(prototype);
        worldMap.setConfigValue(CONFIG_KEY_COLOR_MAP, colorScheme);
        Logger.info("Map selected (Mode {}): {}", selectionMode, worldMap.url());

        return worldMap;
    }
}