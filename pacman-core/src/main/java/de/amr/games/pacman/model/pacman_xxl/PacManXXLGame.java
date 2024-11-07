/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.pacman_xxl;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.MapConfig;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.model.pacman.PacManArcadeGame;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
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
public class PacManXXLGame extends PacManArcadeGame {

    private static final int MAP_COUNT = 8;
    private static final String MAP_PATTERN = "/de/amr/games/pacman/maps/pacman_xxl/masonic_%d.world";

    static final List<Map<String, String>> COLOR_SCHEMES = List.of(
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

    public PacManXXLGame(GameVariant gameVariant, File userDir) {
        super(gameVariant, userDir);
        scoreManager.setHighScoreFile(new File(userDir, "highscore-pacman_xxl.xml"));
        mapSelectionMode = MapSelectionMode.NO_CUSTOM_MAPS;
        for (int num = 1; num <= MAP_COUNT; ++num) {
            URL url = getClass().getResource(MAP_PATTERN.formatted(num));
            standardMaps.add(new WorldMap(url));
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
    public void buildLevel(int levelNumber) {
        currentLevelNumber = levelNumber;
        currentMapConfig = createMapConfig(levelNumber);
        createWorldAndPopulation(currentMapConfig.worldMap());
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUsingAutopilot(false);
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
        setCruiseElroy(0);
        Logger.info("Map selection mode is {}", mapSelectionMode);
        Logger.info("Selected map config: {}, URL: {}", currentMapConfig, currentMapConfig.worldMap().url());
    }

    private MapConfig createMapConfig(int levelNumber) {
        WorldMap map = null;
        Map<String, String> colorScheme;
        switch (mapSelectionMode) {
            case NO_CUSTOM_MAPS -> {
                map = levelNumber <= standardMaps.size()
                    ? standardMaps.get(levelNumber - 1)
                    : standardMaps.get(randomInt(0, standardMaps.size()));
            }
            case CUSTOM_MAPS_FIRST -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(standardMaps);
                map = levelNumber <= maps.size()
                    ? maps.get(levelNumber - 1)
                    : maps.get(randomInt(0, maps.size()));
            }
            case ALL_RANDOM -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(standardMaps);
                map = maps.get(randomInt(0, maps.size()));
            }
        }
        if (standardMaps.contains(map)) {
            // try using random color scheme, TODO: avoid repetitions
            colorScheme = COLOR_SCHEMES.get(randomInt(0, COLOR_SCHEMES.size()));
        } else {
            colorScheme = Map.of(
                "fill",   map.terrain().getPropertyOrDefault(WorldMap.PROPERTY_COLOR_WALL_FILL, "000000"),
                "stroke", map.terrain().getPropertyOrDefault(WorldMap.PROPERTY_COLOR_WALL_STROKE, "0000ff"),
                "door",   map.terrain().getPropertyOrDefault(WorldMap.PROPERTY_COLOR_DOOR, "00ffff"),
                "pellet", map.food().getPropertyOrDefault(WorldMap.PROPERTY_COLOR_FOOD, "ffffff")
            );
        }
        return new MapConfig("Pac-Man XXL Map", 42, map, colorScheme);
    }

    @Override
    public void activateNextBonus() {
        nextBonusIndex += 1;
        byte symbol = bonusSymbols[nextBonusIndex];
        bonus = new StaticBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        // in a non-Arcade style custom map, the bonus position must be taken from the terrain map
        if (world.map().terrain().hasProperty(GameWorld.PROPERTY_POS_BONUS)) {
            Vector2i bonusTile = world.map().terrain().getTileProperty(GameWorld.PROPERTY_POS_BONUS, new Vector2i(13, 20));
            bonus.entity().setPosition(halfTileRightOf(bonusTile));
        } else {
            bonus.entity().setPosition(BONUS_POS);
        }
        bonus.setEdible(bonusEdibleTicks());
        publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
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
            customMapsByFile.put(mapFile, new WorldMap(mapFile));
            Logger.info("Custom map loaded from " + mapFile);
        }
        publishGameEvent(GameEventType.CUSTOM_MAPS_CHANGED);
    }

    @Override
    protected GameWorld createWorld(WorldMap map) {
        var world = new GameWorld(map);
        if (!map.terrain().hasProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE)) {
            Logger.warn("No house min tile found in map!");
            map.terrain().setProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE, TileMap.formatTile(v2i(10, 15)));
        }
        Vector2i topLeftTile = map.terrain().getTileProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE, null);
        world.createArcadeHouse(topLeftTile.x(), topLeftTile.y());
        return world;
    }
}