/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.pacmanxxl;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.model.pacman.PacManGameModel;
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
public class PacManXXLGameModel extends PacManGameModel {

    private static final int NUM_MAPS = 8;
    private static final String MAP_PATH_PATTERN = "/de/amr/games/pacman/maps/masonic/masonic_%d.world";

    private final List<WorldMap> standardMaps = new ArrayList<>();
    private final Map<File, WorldMap> customMapsByFile = new HashMap<>();
    private final File customMapDir;
    private MapSelectionMode mapSelectionMode;

    public PacManXXLGameModel(File userDir) {
        super(userDir);
        initialLives = 3;
        mapSelectionMode = MapSelectionMode.NO_CUSTOM_MAPS;
        customMapDir = new File(userDir, "maps");
        highScoreFile = new File(userDir, "highscore-pacman_xxl.xml");
        for (int num = 1; num <= NUM_MAPS; ++num) {
            URL url = getClass().getResource(MAP_PATH_PATTERN.formatted(num));
            standardMaps.add(new WorldMap(url));
        }
        loadCustomMaps();
    }

    @Override
    public GameVariant variant() {
        return GameVariant.PACMAN_XXL;
    }

    public void setMapSelectionMode(MapSelectionMode mapSelectionMode) {
        this.mapSelectionMode = checkNotNull(mapSelectionMode);
    }

    public MapSelectionMode mapSelectionMode() {
        return mapSelectionMode;
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        WorldMap map = selectMap(levelNumber);
        setWorldAndCreatePopulation(createWorld(map));
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        ghosts[RED_GHOST].setName("Blinky");
        ghosts[PINK_GHOST].setName("Pinky");
        ghosts[CYAN_GHOST].setName("Inky");
        ghosts[ORANGE_GHOST].setName("Clyde");
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
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

    public File customMapDir() {
        return customMapDir;
    }

    public void loadCustomMaps() {
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
    }

    private GameWorld createWorld(WorldMap map) {
        var world = new GameWorld(map);
        //TODO house position should be stored in terrain map
        int houseTopLeftX = map.terrain().numCols() / 2 - 4;
        int houseTopLeftY = map.terrain().numRows() / 2 - 3;
        world.createArcadeHouse(houseTopLeftX, houseTopLeftY);
        return world;
    }
}