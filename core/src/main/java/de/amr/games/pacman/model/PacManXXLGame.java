/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.steering.RuleBasedPacSteering;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.model.GameModel.checkLevelNumber;

/**
 * Extension of Arcade Pac-Man with 8 additional mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Sean Williams</a>).
 */
public class PacManXXLGame extends PacManGame {

    private static GameWorld createWorld(WorldMap map) {
        var world = new GameWorld(map);
        int houseTopLeftX = map.numCols() / 2 - 4;
        int houseTopLeftY = map.numRows() / 2 - 3;
        world.createArcadeHouse(houseTopLeftX, houseTopLeftY);
        return world;
    }

    private final WorldMap[] masonicMaps = new WorldMap[8];
    private List<WorldMap> customMaps = new ArrayList<>();

    @Override
    public void init() {
        initialLives = 3;
        highScoreFile = new File(GAME_DIR,"highscore-pacman_xxl.xml");
        String mapPath = "/de/amr/games/pacman/maps/masonic/";
        for (int number = 1; number <= 8; ++number) {
            URL url = getClass().getResource(mapPath + "masonic_%d.world".formatted(number));
            masonicMaps[number-1] = new WorldMap(url);
        }
    }

    @Override
    public GameVariant variant() {
        return GameVariant.PACMAN_XXL;
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        int numCustomMaps = customMaps.size();
        int mapIndex = levelNumber - 1;
        if (mapIndex < numCustomMaps) {
            setWorldAndCreatePopulation(createWorld(customMaps.get(mapIndex)));
        } else if (mapIndex - numCustomMaps < masonicMaps.length) {
            setWorldAndCreatePopulation(createWorld(masonicMaps[mapIndex - numCustomMaps]));
        } else {
            int randomIndex = Globals.randomInt(0, 8);
            setWorldAndCreatePopulation(createWorld(masonicMaps[randomIndex]));
        }
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
    }

    @Override
    public int intermissionNumber(int levelNumber) {
        return 0;
    }

    public List<WorldMap> customMaps() {
        return customMaps;
    }

    public void setCustomMaps(List<WorldMap> maps) {
        customMaps = maps;
    }
}