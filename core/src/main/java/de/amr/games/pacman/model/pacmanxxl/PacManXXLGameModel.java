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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.halfTileRightOf;
import static de.amr.games.pacman.lib.Globals.randomInt;

/**
 * Extension of Arcade Pac-Man with 8 additional mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Sean Williams</a>).
 */
public class PacManXXLGameModel extends PacManGameModel {

    private final WorldMap[] masonicMaps = new WorldMap[8];
    private final Map<File, WorldMap> customMapsByFile = new HashMap<>();
    private File customMapDir;
    private boolean customMapsEnabled;

    @Override
    public void init(File userDir) {
        initialLives = 3;
        customMapDir = new File(userDir, "maps");
        highScoreFile = new File(userDir, "highscore-pacman_xxl.xml");
        String mapPath = "/de/amr/games/pacman/maps/masonic/";
        for (int number = 1; number <= 8; ++number) {
            URL url = getClass().getResource(mapPath + "masonic_%d.world".formatted(number));
            masonicMaps[number - 1] = new WorldMap(url);
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
            WorldMap map = customMapsSortedByFile().get(mapIndex);
            setWorldAndCreatePopulation(createWorld(map));
        } else if (mapIndex - numCustomMaps < masonicMaps.length) {
            setWorldAndCreatePopulation(createWorld(masonicMaps[mapIndex - numCustomMaps]));
        } else {
            int randomIndex = randomInt(0, masonicMaps.length);
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

    public void setCustomMapsEnabled(boolean customMapsEnabled) {
        this.customMapsEnabled = customMapsEnabled;
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