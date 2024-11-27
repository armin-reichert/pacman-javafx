/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.pacman_xxl;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.model.pacman.PacManGame;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Extension of Arcade Pac-Man with 8 additional mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>).
 */
public class PacManGameXXL extends PacManGame {

    private static final int MAP_COUNT = 8;
    private static final String MAP_PATTERN = "/de/amr/games/pacman/maps/pacman_xxl/masonic_%d.world";

    static final List<Map<String, String>> COLOR_MAPS = List.of(
        Map.of("fill", "#359c9c", "stroke", "#85e2ff", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#c2b853", "stroke", "#ffeace", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#86669c", "stroke", "#f6c4e0", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#ed0a04", "stroke", "#f0b4cd", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#2067c1", "stroke", "#65e5bb", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#c55994", "stroke", "#f760c0", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#12bc76", "stroke", "#ade672", "door", "#fcb5ff", "pellet", "#feb8ae"),
        Map.of("fill", "#5036d9", "stroke", "#5f8bcf", "door", "#fcb5ff", "pellet", "#feb8ae")
    );

    private final List<WorldMap> standardMaps = new ArrayList<>();
    private final Map<File, WorldMap> customMapsByFile = new HashMap<>();
    private MapSelectionMode mapSelectionMode;

    public PacManGameXXL(File userDir) {
        super(userDir);
        scoreManager.setHighScoreFile(new File(userDir, "highscore-pacman_xxl.xml"));
        mapSelectionMode = MapSelectionMode.NO_CUSTOM_MAPS;
        for (int num = 1; num <= MAP_COUNT; ++num) {
            URL url = getClass().getResource(MAP_PATTERN.formatted(num));
            try {
                WorldMap worldMap = new WorldMap(url);
                standardMaps.add(worldMap);
            } catch (IOException x) {
                Logger.error("Could not create world map, url={}", url);
                throw new RuntimeException(x);
            }
        }
        updateCustomMaps();
    }

    public void setMapSelectionMode(MapSelectionMode mapSelectionMode) {
        this.mapSelectionMode = checkNotNull(mapSelectionMode);
        Logger.info("Map selection mode is now {}", mapSelectionMode);
    }

    public MapSelectionMode mapSelectionMode() {
        return mapSelectionMode;
    }

    @Override
    protected void populateLevel(WorldMap worldMap) {
        super.populateLevel(worldMap);
        if (!worldMap.terrain().hasProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE)) {
            Logger.warn("No house min tile found in map!");
            worldMap.terrain().setProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE, TileMap.formatTile(v2i(10, 15)));
        }
        Vector2i topLeftTile = worldMap.terrain().getTileProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE, null);
        level.world().createArcadeHouse(topLeftTile.x(), topLeftTile.y());
    }

    @Override
    public void configureNormalLevel() {
        levelCounterEnabled = true;
        Map<String, Object> mapConfig = createMapConfig(level.number);
        level.setMapConfig(mapConfig);
        level.setNumFlashes(levelData(level.number).numFlashes());
        level.setIntermissionNumber(intermissionNumberAfterLevel(level.number));
        WorldMap worldMap = (WorldMap) mapConfig.get("worldMap");
        populateLevel(worldMap);
        level.pac().setAutopilot(autopilot);
        setCruiseElroy(0);
        level.ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));

        Logger.info("Map config: {}, URL: {}", mapConfig, worldMap.url());
    }

    private Map<String, Object> createMapConfig(int levelNumber) {
        WorldMap worldMap = null;
        Map<String, String> colorMap;
        switch (mapSelectionMode) {
            case NO_CUSTOM_MAPS -> {
                worldMap = levelNumber <= standardMaps.size()
                    ? standardMaps.get(levelNumber - 1)
                    : standardMaps.get(randomInt(0, standardMaps.size()));
            }
            case CUSTOM_MAPS_FIRST -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(standardMaps);
                worldMap = levelNumber <= maps.size()
                    ? maps.get(levelNumber - 1)
                    : maps.get(randomInt(0, maps.size()));
            }
            case ALL_RANDOM -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(standardMaps);
                worldMap = maps.get(randomInt(0, maps.size()));
            }
        }
        if (standardMaps.contains(worldMap)) {
            // try using random color scheme, TODO: avoid repetitions
            colorMap = COLOR_MAPS.get(randomInt(0, COLOR_MAPS.size()));
        } else {
            colorMap = Map.of(
                "fill",   worldMap.terrain().getPropertyOrDefault(WorldMap.PROPERTY_COLOR_WALL_FILL, "000000"),
                "stroke", worldMap.terrain().getPropertyOrDefault(WorldMap.PROPERTY_COLOR_WALL_STROKE, "0000ff"),
                "door",   worldMap.terrain().getPropertyOrDefault(WorldMap.PROPERTY_COLOR_DOOR, "00ffff"),
                "pellet", worldMap.food().getPropertyOrDefault(WorldMap.PROPERTY_COLOR_FOOD, "ffffff")
            );
        }
        return Map.of("worldMap", worldMap, "colorMap", colorMap);
    }

    @Override
    public void activateNextBonus() {
        level.advanceNextBonus();
        byte symbol = level.bonusSymbol(level.nextBonusIndex());
        StaticBonus staticBonus = new StaticBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        level.setBonus(staticBonus);
        // in a non-Arcade style custom map, the bonus position must be taken from the terrain map
        if (level.world().map().terrain().hasProperty(GameWorld.PROPERTY_POS_BONUS)) {
            Vector2i bonusTile = level.world().map().terrain().getTileProperty(GameWorld.PROPERTY_POS_BONUS, new Vector2i(13, 20));
            staticBonus.entity().setPosition(halfTileRightOf(bonusTile));
        } else {
            staticBonus.entity().setPosition(BONUS_POS);
        }
        staticBonus.setEdible(bonusEdibleTicks());
        publishGameEvent(GameEventType.BONUS_ACTIVATED, staticBonus.entity().tile());
    }

    public Map<File, WorldMap> customMapsByFile() {
        return customMapsByFile;
    }

    public List<WorldMap> customMapsSortedByFile() {
        return customMapsByFile.keySet().stream().sorted().map(customMapsByFile::get).toList();
    }

    @Override
    public void updateCustomMaps() {
        if (customMapDir.exists() && customMapDir.isDirectory()) {
            Logger.info("Custom map directory found: '{}'", customMapDir);
        } else {
            if (customMapDir.mkdirs()) {
                Logger.info("Custom map directory created: '{}'", customMapDir);
            } else {
                Logger.error("Custom map directory could not be created: '{}'", customMapDir);
                return;
            }
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
                WorldMap worldMap = new WorldMap(mapFile);
                customMapsByFile.put(mapFile, worldMap);
                Logger.info("Custom map loaded from file {}", mapFile);
            } catch (IOException x) {
                Logger.error(x);
                Logger.error("Could not read custom map from file {}", mapFile);
            }
        }
        publishGameEvent(GameEventType.CUSTOM_MAPS_CHANGED);
    }
}