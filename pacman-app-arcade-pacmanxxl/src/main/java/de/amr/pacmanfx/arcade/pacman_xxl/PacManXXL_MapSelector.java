/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.CONFIG_KEY_COLOR_MAP;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class PacManXXL_MapSelector implements WorldMapSelector, DirectoryWatchdog.WatchEventListener {

    public static final List<WorldMapColorScheme> MAP_COLOR_SCHEMES = List.of(
        new WorldMapColorScheme("#359c9c", "#85e2ff", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#c2b853", "#ffeace", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#86669c", "#f6c4e0", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#ed0a04", "#f0b4cd", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#2067c1", "#65e5bb", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#c55994", "#f760c0", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#12bc76", "#ade672", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#5036d9", "#5f8bcf", "#fcb5ff", "#feb8ae")
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
                URL url = PacManXXL_MapSelector.class.getResource(path);
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
    private WorldMapSelectionMode selectionMode;

    public PacManXXL_MapSelector(File dir) {
        customMapDir = requireNonNull(dir);
        selectionMode = WorldMapSelectionMode.CUSTOM_MAPS_FIRST;
        addSampleCustomMapPrototypes(dir);
    }

    public WorldMapSelectionMode selectionMode() {
        return selectionMode;
    }

    @Override
    public void handleWatchEvents(List<WatchEvent<Path>> events) {
        if (!events.isEmpty()) {
            Logger.info("Detected custom map directory changes:");
            for (WatchEvent<Path> event : events) {
                final Path path = event.context();
                final File worldMapFile = new File(customMapDir, path.toFile().getPath());
                Logger.info("\t{}, path='{}'", event.kind(), worldMapFile);
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    try {
                        final WorldMap worldMap = WorldMap.loadFromFile(worldMapFile);
                        customMapPrototypes.add(worldMap);
                        Logger.info("Added custom map {}", worldMapFile);
                    } catch (IOException x) {
                        Logger.error(x);
                    }
                }
                else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    try {
                        final URL url = worldMapFile.toURI().toURL();
                        findCustomMapPrototype(url).ifPresent(worldMap -> {
                            customMapPrototypes.remove(worldMap);
                            Logger.info("Removed custom map {}", worldMapFile);
                        });
                    } catch (MalformedURLException x) {
                        Logger.error(x);
                    }
                }
                else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    try {
                        final URL url = worldMapFile.toURI().toURL();
                        findCustomMapPrototype(url).ifPresent(customMapPrototypes::remove);
                        final WorldMap worldMap = WorldMap.loadFromFile(worldMapFile);
                        customMapPrototypes.add(worldMap);
                        Logger.info("Updated custom map {}", worldMapFile);
                    } catch (IOException x) {
                        Logger.error(x);
                    }
                }
            }
        }
    }

    private Optional<WorldMap> findCustomMapPrototype(URL url) {
        return customMapPrototypes.stream().filter(worldMap -> url.toString().equals(worldMap.url())).findFirst();
    }

    public void setSelectionMode(WorldMapSelectionMode mode) {
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
        File[] mapFiles = customMapDir.listFiles((_, name) -> name.endsWith(".world"));
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
            List<WorldMap> maps = WorldMapSelector.loadMapsFromModule(getClass(), "maps/masonic_%d.world", 8);
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
            : WorldMapSelector.extractColorMap(prototype);
        worldMap.setConfigValue(CONFIG_KEY_COLOR_MAP, colorScheme);
        Logger.info("Map selected (Mode {}): {}", selectionMode, worldMap.url());

        return worldMap;
    }
}