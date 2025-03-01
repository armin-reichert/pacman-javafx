package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.tilemap.LayerID;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.MapSelector;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.assertNotNull;
import static de.amr.games.pacman.lib.Globals.randomInt;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.lib.tilemap.WorldMap.PROPERTY_COLOR_FOOD;
import static de.amr.games.pacman.model.GameModel.CUSTOM_MAP_DIR;

public class PacManXXL_MapSelector implements MapSelector {

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

    private List<WorldMap> builtinMaps = new ArrayList<>();
    private final Map<File, WorldMap> customMapsByFile = new HashMap<>();
    private MapSelectionMode mapSelectionMode = MapSelectionMode.CUSTOM_MAPS_FIRST;

    @Override
    public void loadAllMaps(GameModel game) {
        builtinMaps = loadMapsFromModule("maps/masonic_%d.world", 8);
        updateCustomMaps(game);
    }

    @Override
    public WorldMap selectWorldMap(int levelNumber) {
        WorldMap template = switch (mapSelectionMode()) {
            case NO_CUSTOM_MAPS ->
                    levelNumber <= builtinMaps.size()
                            ? builtinMaps.get(levelNumber - 1)
                            : builtinMaps.get(randomInt(0, builtinMaps.size()));
            case CUSTOM_MAPS_FIRST -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(builtinMaps);
                yield levelNumber <= maps.size()
                        ? maps.get(levelNumber - 1)
                        : maps.get(randomInt(0, maps.size()));
            }
            case ALL_RANDOM -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(builtinMaps);
                yield maps.get(randomInt(0, maps.size()));
            }
        };

        WorldMap worldMap = new WorldMap(template);
        Map<String, String> mapColoring = builtinMaps.contains(template) ? randomMapColoring() : coloringFromMap(template);
        worldMap.setConfigValue("colorMap", mapColoring);

        return worldMap;
    }

    public void setMapSelectionMode(MapSelectionMode mapSelectionMode) {
        this.mapSelectionMode = assertNotNull(mapSelectionMode);
    }

    public MapSelectionMode mapSelectionMode() {
        return mapSelectionMode;
    }

    public Map<File, WorldMap> customMapsByFile() {
        return customMapsByFile;
    }

    public List<WorldMap> customMapsSortedByFile() {
        return customMapsByFile.keySet().stream().sorted().map(customMapsByFile::get).toList();
    }

    public void updateCustomMaps(GameModel game) {
        if (mapSelectionMode == MapSelectionMode.NO_CUSTOM_MAPS) {
            return;
        }
        File[] mapFiles = CUSTOM_MAP_DIR.listFiles((dir, name) -> name.endsWith(".world"));
        if (mapFiles == null) {
            Logger.error("An error occurred accessing custom map directory {}", CUSTOM_MAP_DIR);
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
                WorldMap worldMap = new WorldMap(mapFile);
                customMapsByFile.put(mapFile, worldMap);
                Logger.info("Custom map loaded from file {}", mapFile);
            } catch (IOException x) {
                Logger.error(x);
                Logger.error("Could not read custom map from file {}", mapFile);
            }
        }
        game.publishGameEvent(GameEventType.CUSTOM_MAPS_CHANGED);
    }

    private Map<String, String> randomMapColoring() {
        return MAP_COLORINGS.get(randomInt(0, MAP_COLORINGS.size()));
    }

    private Map<String, String> coloringFromMap(WorldMap template) {
        return Map.of(
                "fill",   template.getPropertyOrDefault(LayerID.TERRAIN, PROPERTY_COLOR_WALL_FILL,   "000000"),
                "stroke", template.getPropertyOrDefault(LayerID.TERRAIN, PROPERTY_COLOR_WALL_STROKE, "0000ff"),
                "door",   template.getPropertyOrDefault(LayerID.TERRAIN, PROPERTY_COLOR_DOOR,        "00ffff"),
                "pellet", template.getPropertyOrDefault(LayerID.FOOD, PROPERTY_COLOR_FOOD,           "ffffff"));
    }

}
