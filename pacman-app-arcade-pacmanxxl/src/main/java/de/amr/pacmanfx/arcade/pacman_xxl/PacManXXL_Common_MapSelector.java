package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.MapSelector;
import de.amr.pacmanfx.model.WorldMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static java.util.Objects.requireNonNull;

public class PacManXXL_Common_MapSelector implements MapSelector {

    private static final List<Map<String, String>> MAP_COLORINGS = List.of(
            Map.of("fill", "#359c9c", "stroke", "#85e2ff", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#c2b853", "stroke", "#ffeace", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#86669c", "stroke", "#f6c4e0", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#ed0a04", "stroke", "#f0b4cd", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#2067c1", "stroke", "#65e5bb", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#c55994", "stroke", "#f760c0", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#12bc76", "stroke", "#ade672", "door", "#fcb5ff", "pellet", "#feb8ae"),
            Map.of("fill", "#5036d9", "stroke", "#5f8bcf", "door", "#fcb5ff", "pellet", "#feb8ae")
    );

    private final File customMapDir;
    private List<WorldMap> builtinMaps = new ArrayList<>();
    private final ObservableList<WorldMap> customMapsByFile = FXCollections.observableList(new ArrayList<>());
    private final DirectoryWatchdog goodBoy;
    private boolean customMapsUpToDate;
    private MapSelectionMode mapSelectionMode;

    public PacManXXL_Common_MapSelector(File customMapDir) {
        this.customMapDir = requireNonNull(customMapDir);
        goodBoy = new DirectoryWatchdog(customMapDir);
        mapSelectionMode = MapSelectionMode.CUSTOM_MAPS_FIRST;
        customMapsUpToDate = false;
        goodBoy.setEventConsumer(eventList -> {
            Logger.info("Custom map change(s) detected: {}",
                eventList.stream()
                    .map(watchEvent -> String.format("%s: '%s'", watchEvent.kind(), watchEvent.context()))
                    .toList());
            setCustomMapsUpToDate(false);
            loadCustomMaps();
        });
    }

    public MapSelectionMode mapSelectionMode() {
        return mapSelectionMode;
    }

    public void setMapSelectionMode(MapSelectionMode mapSelectionMode) {
        this.mapSelectionMode = requireNonNull(mapSelectionMode);
    }

    public void startWatchingCustomMaps() {
        goodBoy.startWatching();
    }

    public void setCustomMapsUpToDate(boolean customMapsUpToDate) {
        this.customMapsUpToDate = customMapsUpToDate;
    }

    public boolean areCustomMapsUpToDate() {
        return customMapsUpToDate;
    }

    @Override
    public List<WorldMap> builtinMaps() {
        return builtinMaps;
    }

    @Override
    public ObservableList<WorldMap> customMaps() {
        return customMapsByFile;
    }

    @Override
    public void loadCustomMaps() {
        if (customMapsUpToDate) {
            Logger.info("Custom maps not loaded as they are up-to-date");
            return;
        }
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
        customMapsByFile.clear();
        for (File mapFile : mapFiles) {
            try {
                WorldMap worldMap = WorldMap.fromFile(mapFile);
                customMapsByFile.add(worldMap);
                Logger.info("Custom map loaded from file {}", mapFile);
            } catch (IOException x) {
                Logger.error(x);
                Logger.error("Could not read custom map from file {}", mapFile);
            }
        }
        customMapsUpToDate = true;
    }

    @Override
    public void loadAllMaps() {
        if (builtinMaps.isEmpty()) {
            builtinMaps = MapSelector.loadMapsFromModule(getClass(), "maps/masonic_%d.world", 8);
        }
        loadCustomMaps();
    }

    @Override
    public WorldMap findWorldMap(int levelNumber) {
        WorldMap template = switch (mapSelectionMode) {
            case NO_CUSTOM_MAPS ->
                    levelNumber <= builtinMaps.size()
                            ? builtinMaps.get(levelNumber - 1)
                            : builtinMaps.get(randomInt(0, builtinMaps.size()));
            case CUSTOM_MAPS_FIRST -> {
                List<WorldMap> maps = new ArrayList<>(customMaps());
                maps.addAll(builtinMaps);
                yield levelNumber <= maps.size()
                        ? maps.get(levelNumber - 1)
                        : maps.get(randomInt(0, maps.size()));
            }
            case ALL_RANDOM -> {
                List<WorldMap> maps = new ArrayList<>(customMaps());
                maps.addAll(builtinMaps);
                yield maps.get(randomInt(0, maps.size()));
            }
        };

        WorldMap worldMap = WorldMap.copyMap(template);
        Map<String, String> mapColoring = builtinMaps.contains(template) ? randomMapColoring() : coloringFromMap(template);
        worldMap.setConfigValue("colorMap", mapColoring);

        Logger.info("Map selected (Mode {}): {}", mapSelectionMode, worldMap.url());
        return worldMap;
    }

    private Map<String, String> randomMapColoring() {
        return MAP_COLORINGS.get(randomInt(0, MAP_COLORINGS.size()));
    }

    private Map<String, String> coloringFromMap(WorldMap template) {
        return Map.of(
            "fill",   template.properties(LayerID.TERRAIN).getOrDefault(WorldMapProperty.COLOR_WALL_FILL,   "000000"),
            "stroke", template.properties(LayerID.TERRAIN).getOrDefault(WorldMapProperty.COLOR_WALL_STROKE, "0000ff"),
            "door",   template.properties(LayerID.TERRAIN).getOrDefault(WorldMapProperty.COLOR_DOOR,        "00ffff"),
            "pellet", template.properties(LayerID.FOOD).getOrDefault(WorldMapProperty.COLOR_FOOD,           "ffffff"));
    }
}