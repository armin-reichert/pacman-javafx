/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.lib.PathWatchEventListener;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.ui.GameUI_Config;
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
import java.util.Optional;

import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class PacManXXL_MapSelector implements WorldMapSelector, PathWatchEventListener {

    public static final WorldMapColorScheme[] WORLD_MAP_COLOR_SCHEMES = {
        new WorldMapColorScheme("#359c9c", "#85e2ff", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#c2b853", "#ffeace", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#86669c", "#f6c4e0", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#ed0a04", "#f0b4cd", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#2067c1", "#65e5bb", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#c55994", "#f760c0", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#12bc76", "#ade672", "#fcb5ff", "#feb8ae"),
        new WorldMapColorScheme("#5036d9", "#5f8bcf", "#fcb5ff", "#feb8ae")
    };

    private final File customMapDir;
    private final ObservableList<WorldMap> customMapPrototypes = FXCollections.observableArrayList();
    private final List<WorldMap> builtinMapPrototypes = new ArrayList<>();
    private WorldMapSelectionMode selectionMode;

    public PacManXXL_MapSelector(File customMapDir) {
        this.customMapDir = requireNonNull(customMapDir);
        this.selectionMode = WorldMapSelectionMode.CUSTOM_MAPS_FIRST;
        addJuniorPacMapPrototypesIfEmptyDir(customMapDir);
    }

    public WorldMapSelectionMode selectionMode() {
        return selectionMode;
    }

    @Override
    public void handleWatchEvents(List<WatchEvent<Path>> watchEvents) {
        Logger.info("Detected custom map directory changes:");
        for (WatchEvent<Path> event : watchEvents) {
            final Path relPath = event.context(); // file or directory name in custom map dir
            final File file = customMapDir.toPath().resolve(relPath).toFile();
            Logger.info("WatchEvent kind={}, relative path='{}' file='{}'", event.kind(), relPath, file);
            if (!file.getAbsolutePath().toLowerCase().endsWith(".world")) {
                Logger.info("Ignored: File '{}' is no world map file or has wrong extension", file);
                continue;
            }
            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                // A new map file has appeared
                try {
                    final WorldMap worldMap = WorldMap.loadFromFile(file);
                    customMapPrototypes.add(worldMap);
                    Logger.info("Added new custom map from file '{}'", file);
                } catch (IOException x) {
                    Logger.error("Could not load world map");
                    Logger.error(x);
                }
                catch (WorldMapParseException x) {
                    Logger.error("Could not parse world map");
                    Logger.error(x);
                }
            }
            else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                // A map file has been removed
                try {
                    final URL url = file.toURI().toURL();
                    findCustomMapPrototype(url).ifPresent(worldMap -> {
                        customMapPrototypes.remove(worldMap);
                        Logger.info("Removed custom map file '{}'", file);
                    });
                } catch (MalformedURLException x) {
                    Logger.error("Could not remove custom map for deleted file '{}'", file);
                    Logger.error(x);
                }
            }
            else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                // A map file has been changed
                try {
                    final URL url = file.toURI().toURL();
                    findCustomMapPrototype(url).ifPresent(customMapPrototypes::remove);
                    final WorldMap worldMap = WorldMap.loadFromFile(file);
                    customMapPrototypes.add(worldMap);
                    Logger.info("Updated custom map from file '{}'", file);
                } catch (IOException x) {
                    Logger.error("Could not load world map");
                    Logger.error(x);
                }
                catch (WorldMapParseException x) {
                    Logger.error("Could not parse world map");
                    Logger.error(x);
                }
            }
        }
    }

    public void setSelectionMode(WorldMapSelectionMode mode) {
        selectionMode = requireNonNull(mode);
    }

    @Override
    public ObservableList<WorldMap> customMapPrototypes() {
        return customMapPrototypes;
    }

    @Override
    public void loadCustomMaps() throws IOException, WorldMapParseException {
        if (!customMapPrototypes.isEmpty()) {
            Logger.info("Custom maps have already been loaded");
            return;
        }
        final File[] worldMapFiles = customMapDir.listFiles((_, name) -> name.endsWith(".world"));
        if (worldMapFiles == null) {
            Logger.error("Could not access custom map directory '{}'", customMapDir);
            return;
        }
        if (worldMapFiles.length == 0) {
            Logger.info("No custom maps found in directory '{}'", customMapDir);
        } else {
            Logger.info("Found {} custom map(s)", worldMapFiles.length);
        }
        for (File file : worldMapFiles) {
            final WorldMap worldMap = WorldMap.loadFromFile(file);
            customMapPrototypes.add(worldMap);
            Logger.info("Custom map loaded from file '{}'", file);
        }
    }

    @Override
    public void loadMapPrototypes() {
        if (builtinMapPrototypes.isEmpty()) {
            try {
                final List<WorldMap> predefinedMaps = WorldMapSelector.loadMaps(getClass(), "maps/masonic_%d.world", 8);
                builtinMapPrototypes.addAll(predefinedMaps);
                loadCustomMaps();
            } catch (IOException x) {
                Logger.error("Could not open world map");
                throw new RuntimeException(x);
            }
            catch (WorldMapParseException x) {
                Logger.error("Could not parse world map");
                throw new RuntimeException(x);
            }
        }
    }

    @Override
    public WorldMap supplyWorldMap(int levelNumber, Object... args) {
        loadMapPrototypes(); // ensure maps loaded
        final WorldMap prototype = switch (selectionMode) {
            case NO_CUSTOM_MAPS -> {
                // first pick built-in maps in order, then randomly
                final int index = levelNumber <= builtinMapPrototypes.size()
                    ? levelNumber - 1 : randomInt(0, builtinMapPrototypes.size());
                yield builtinMapPrototypes.get(index);
            }
            case CUSTOM_MAPS_FIRST -> {
                if (levelNumber <= customMapPrototypes.size()) {
                    // pick custom maps in order
                    yield customMapPrototypes.get(levelNumber - 1);
                }
                else {
                    // pick random built-in map
                    yield builtinMapPrototypes.get(randomInt(0, builtinMapPrototypes.size()));
                }
            }
            case ALL_RANDOM -> {
                final int index = randomInt(0, customMapPrototypes().size() + builtinMapPrototypes.size());
                yield index < customMapPrototypes().size() ? customMapPrototypes.get(index) : builtinMapPrototypes.get(index - customMapPrototypes().size());
            }
        };

        // Create copy (maps are mutable!)
        final var worldMap = new WorldMap(prototype);

        // If selected map is a built-in map, use a random color scheme to get variation
        final WorldMapColorScheme colorScheme = builtinMapPrototypes.contains(prototype)
            ? WORLD_MAP_COLOR_SCHEMES[randomInt(0, WORLD_MAP_COLOR_SCHEMES.length)]
            : WorldMapSelector.extractColorScheme(prototype);
        worldMap.setConfigValue(GameUI_Config.ConfigKey.COLOR_SCHEME, colorScheme);
        Logger.info("Map selected (mode {}): {}", selectionMode, worldMap.url());

        return worldMap;
    }

    private void addJuniorPacMapPrototypesIfEmptyDir(File customMapDir) {
        requireNonNull(customMapDir);
        final File[] files = customMapDir.listFiles();
        if (files == null) {
            Logger.error("Could not access custom map directory '{}'", customMapDir);
            return;
        }
        if (files.length == 0) {
            Logger.info("Custom map directory is empty, fill with Junior Pac-Man maps...");
            for (int i = 1; i <= 15; ++i) {
                final String mapName = "Jr. Pac-Man %02d.world".formatted(i);
                final String path = "/de/amr/pacmanfx/arcade/pacman_xxl/maps/junior_pacman/" + mapName;
                final URL url = PacManXXL_MapSelector.class.getResource(path);
                if (url != null) {
                    final File targetFile = new File(customMapDir, mapName);
                    try {
                        WorldMap map = WorldMap.loadFromURL(url);
                        map.saveToFile(targetFile);
                    } catch (IOException e) {
                        Logger.error("Could not save map with URL {} to file ''", url, targetFile);
                    }
                    catch (WorldMapParseException x) {
                        Logger.error("Could not parse world map");
                        Logger.error(x); //TODO
                    }
                } else {
                    // Not all of these maps exits, just log them
                    Logger.warn("Could not access map with path '{}'", path);
                }
            }
        }
    }

    private Optional<WorldMap> findCustomMapPrototype(URL url) {
        return customMapPrototypes.stream().filter(worldMap -> url.toString().equals(worldMap.url())).findFirst();
    }
}