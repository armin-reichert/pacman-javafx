/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.pacmanxxl;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.model.pacman.PacManGameModel;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension of Arcade Pac-Man with 8 additional mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Sean Williams</a>).
 */
public class PacManXXLGameModel extends PacManGameModel {

    private static GameWorld createWorld(WorldMap map) {
        var world = new GameWorld(map);
        //TODO house position should be stored in terrain map
        int houseTopLeftX = map.terrain().numCols() / 2 - 4;
        int houseTopLeftY = map.terrain().numRows() / 2 - 3;
        world.createArcadeHouse(houseTopLeftX, houseTopLeftY);
        return world;
    }

    private final WorldMap[] masonicMaps = new WorldMap[8];
    private final Map<File, WorldMap> customMapsByFile = new HashMap<>();
    private boolean customMapsEnabled;

    @Override
    public void init() {
        initialLives = 3;
        highScoreFile = new File(USER_DIR,"highscore-pacman_xxl.xml");
        String mapPath = "/de/amr/games/pacman/maps/masonic/";
        for (int number = 1; number <= 8; ++number) {
            URL url = getClass().getResource(mapPath + "masonic_%d.world".formatted(number));
            masonicMaps[number-1] = new WorldMap(url);
        }
        loadCustomMaps();
    }

    @Override
    public GameVariant variant() {
        return GameVariant.PACMAN_XXL;
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        int numCustomMaps = customMapsEnabled ? customMapsByFile().size() : 0;
        int mapIndex = levelNumber - 1;
        if (mapIndex < numCustomMaps) {
            //TODO improve
            var customMapFiles = customMapsByFile.keySet().stream().sorted().toList();
            WorldMap map = customMapsByFile.get(customMapFiles.get(mapIndex));
            setWorldAndCreatePopulation(createWorld(map));
        } else if (mapIndex - numCustomMaps < masonicMaps.length) {
            setWorldAndCreatePopulation(createWorld(masonicMaps[mapIndex - numCustomMaps]));
        } else {
            int randomIndex = Globals.randomInt(0, 8);
            setWorldAndCreatePopulation(createWorld(masonicMaps[randomIndex]));
        }
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
    }

    @Override
    public void activateNextBonus() {
        nextBonusIndex += 1;
        byte symbol = bonusSymbols[nextBonusIndex];
        bonus = new StaticBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        // in a non-Arcade style custom map, the bonus position must be taken from the terrain map
        if (world.map().terrain().hasProperty(GameWorld.PROPERTY_POS_BONUS)) {
            bonus.entity().setPosition(world.map().terrain().getProperty(GameWorld.PROPERTY_POS_BONUS));
        } else {
            bonus.entity().setPosition(BONUS_POS);
        }
        //TODO in large maps this could be too short:
        bonus.setEdible(bonusEdibleTicks());
        publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
    }

    @Override
    public int intermissionNumber(int levelNumber) {
        return 0;
    }

    public List<WorldMap> customMaps() {
        var customMapFiles = customMapsByFile.keySet().stream().sorted().toList();
        return customMapFiles.stream().map(customMapsByFile::get).toList();
    }

    public void setCustomMapsEnabled(boolean customMapsEnabled) {
        this.customMapsEnabled = customMapsEnabled;
    }

    public void loadCustomMaps() {
        ensureCustomMapDirExists();
        File mapDir = GameModel.CUSTOM_MAP_DIR;
        if (!mapDir.isDirectory()) {
            Logger.error("Cannot load custom maps: '{}' is not a directory", mapDir);
            return;
        }
        Logger.info("Searching for custom map files in '{}'", mapDir);
        File[] mapFiles = mapDir.listFiles((dir, name) -> name.endsWith(".world"));
        if (mapFiles == null) {
            Logger.error("An error occurred on accessing custom map folder {}", mapDir);
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
            Logger.info("Created custom map from file: " + mapFile);
        }
    }

    private void ensureCustomMapDirExists() {
        var dir = GameModel.CUSTOM_MAP_DIR;
        if (dir.exists() && dir.isDirectory()) {
            Logger.info("Custom map directory found: '{}'", dir);
            return;
        }
        boolean created = dir.mkdirs();
        if (created) {
            Logger.info("Custom map directory created: '{}'", dir);
        } else {
            Logger.error("Custom map directory could not be created: '{}'", dir);
        }
    }

    public Map<File, WorldMap> customMapsByFile() {
        return customMapsByFile;
    }
}