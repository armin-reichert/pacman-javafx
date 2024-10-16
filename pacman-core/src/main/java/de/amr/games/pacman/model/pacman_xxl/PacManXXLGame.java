/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.pacman_xxl;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.MapColorScheme;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
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
    private static final String MAP_PATTERN = "/de/amr/games/pacman/maps/masonic/masonic_%d.world";

    static final MapColorScheme[] COLOR_SCHEMES = {
        new MapColorScheme("#359c9c", "#85e2ff", "#fcb5ff", "#feb8ae"),
        new MapColorScheme("#c2b853", "#ffeace", "#fcb5ff", "#feb8ae"),
        new MapColorScheme("#86669c", "#f6c4e0", "#fcb5ff", "#feb8ae"),
        new MapColorScheme("#ed0a04", "#f0b4cd", "#fcb5ff", "#feb8ae"),
        new MapColorScheme("#2067c1", "#65e5bb", "#fcb5ff", "#feb8ae"),
        new MapColorScheme("#c55994", "#f760c0", "#fcb5ff", "#feb8ae"),
        new MapColorScheme("#12bc76", "#ade672", "#fcb5ff", "#feb8ae"),
        new MapColorScheme("#5036d9", "#5f8bcf", "#fcb5ff", "#feb8ae"),
    };

    private final List<WorldMap> standardMaps = new ArrayList<>();
    private final Map<File, WorldMap> customMapsByFile = new HashMap<>();
    private final File customMapDir;
    private MapSelectionMode mapSelectionMode;

    public PacManXXLGame(GameVariant gameVariant, File userDir) {
        super(gameVariant, userDir);
        initialLives = 3;
        mapSelectionMode = MapSelectionMode.NO_CUSTOM_MAPS;
        customMapDir = new File(userDir, "maps");
        highScoreFile = new File(userDir, "highscore-pacman_xxl.xml");
        for (int num = 1; num <= MAP_COUNT; ++num) {
            URL url = getClass().getResource(MAP_PATTERN.formatted(num));
            standardMaps.add(new WorldMap(url));
        }
        updateCustomMaps();
    }

    @Override
    public int mapCount() {
        return MAP_COUNT;
    }

    public void setMapSelectionMode(MapSelectionMode mapSelectionMode) {
        this.mapSelectionMode = checkNotNull(mapSelectionMode);
        Logger.info("Map selection mode is now {}", mapSelectionMode);
    }

    public MapSelectionMode mapSelectionMode() {
        return mapSelectionMode;
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = levelNumber;
        WorldMap map = selectMap(levelNumber);
        if (standardMaps.contains(map)) {
            // try using random color scheme, TODO: avoid repetitions
            int index = randomInt(0, COLOR_SCHEMES.length);
            map.setColorScheme(COLOR_SCHEMES[index]);
        }
        Logger.info("Map selection mode is {}", mapSelectionMode);
        Logger.info("Selected map URL is {}", map.url());
        Logger.info("Selected map colors: {}", map.colorScheme());
        setWorldAndCreatePopulation(createWorld(map));
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    public int mapNumberByLevelNumber(int levelNumber) {
        return 42; //TODO
    }

    private WorldMap selectMap(int levelNumber) {
        switch (mapSelectionMode) {
            case NO_CUSTOM_MAPS -> {
                return levelNumber <= standardMaps.size()
                    ? standardMaps.get(levelNumber - 1)
                    : standardMaps.get(randomInt(0, standardMaps.size()));
            }
            case CUSTOM_MAPS_FIRST -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(standardMaps);
                return levelNumber <= maps.size()
                    ? maps.get(levelNumber - 1)
                    : maps.get(randomInt(0, maps.size()));
            }
            case ALL_RANDOM -> {
                List<WorldMap> maps = new ArrayList<>(customMapsSortedByFile());
                maps.addAll(standardMaps);
                return maps.get(randomInt(0, maps.size()));
            }
        }
        throw new IllegalStateException("Illegal map selection mode " + mapSelectionMode);
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

    @Override
    public int intermissionNumber(int levelNumber) {
        return 0;
    }

    public Map<File, WorldMap> customMapsByFile() {
        return customMapsByFile;
    }

    public List<WorldMap> customMapsSortedByFile() {
        return customMapsByFile.keySet().stream().sorted().map(customMapsByFile::get).toList();
    }

    @Override
    public File customMapDir() {
        return customMapDir;
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

    private GameWorld createWorld(WorldMap map) {
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