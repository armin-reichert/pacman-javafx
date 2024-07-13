/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.steering.RuleBasedPacSteering;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.checkLevelNumber;

/**
 * Extension of Arcade Pac-Man with 8 additional mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Sean Williams</a>).
 */
public class PacManXXLGame extends PacManGame {

    private WorldMap[] worldMapByLevelNumber = new WorldMap[8];

    @Override
    public void init() {
        initialLives = 3;
        highScoreFile = new File(GAME_DIR,"highscore-pacman_xxl.xml");
        String mapPath = "/de/amr/games/pacman/maps/masonic/";
        for (int number = 1; number <= 8; ++number) {
            URL url = getClass().getResource(mapPath + "masonic_%d.world".formatted(number));
            worldMapByLevelNumber[number-1] = new WorldMap(url);
        }
    }

    @Override
    public GameVariant variant() {
        return GameVariant.PACMAN_XXL;
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        if (levelNumber <= worldMapByLevelNumber.length) {
            setWorldAndCreatePopulation(createWorld(worldMapByLevelNumber[levelNumber-1]));
        } else {
            int randomIndex = Globals.randomInt(0, 8);
            setWorldAndCreatePopulation(createWorld(worldMapByLevelNumber[randomIndex]));
        }
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
    }

    @Override
    public int intermissionNumber(int levelNumber) {
        return 0;
    }

    private World createWorld(WorldMap map) {
        var world = new World(map);
        int topLeftX = map.numCols() / 2 - 4;
        int topLeftY = map.numRows() / 2 - 3;
        House house = House.createArcadeHouse(v2i(topLeftX, topLeftY));
        house.setPacPositionFromMap(map);
        house.setGhostPositionsFromMap(map);
        world.addHouse(house);
        world.setBonusPosition(halfTileRightOf(13, 20)); // TODO get position from map?
        return world;
    }
}