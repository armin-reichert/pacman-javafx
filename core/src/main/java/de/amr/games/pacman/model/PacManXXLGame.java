/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.steering.RuleBasedPacSteering;

import java.io.File;
import java.net.URL;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.checkLevelNumber;

/**
 * Extension of Arcade Pac-Man with 8 additional mazes (thanks to the one and only Sean Williams!).
 */
public class PacManXXLGame extends PacManGame {

    @Override
    public void init() {
        initialLives = 3;
        highScoreFile = new File(GAME_DIR,"highscore-pacman_xxl.xml");
    }

    @Override
    public GameVariant variant() {
        return GameVariant.PACMAN_XXL;
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        var customMaps = GameController.it().getCustomMaps();
        var world = switch (levelNumber) {
            case 1 -> customMaps.isEmpty() ? createPacManWorld() : createModernWorld(customMaps.get(0));
            case 2, 3, 4, 5, 6, 7, 8, 9 -> {
                int mapNumber = levelNumber - 1;
                URL mapURL = getClass().getResource(String.format("/de/amr/games/pacman/maps/masonic/masonic_%d.world", mapNumber));
                var map = new WorldMap(mapURL);
                yield createModernWorld(map);
            }
            default -> {
                int mapNumber = randomInt(1, 9);
                URL mapURL = getClass().getResource(String.format("/de/amr/games/pacman/maps/masonic/masonic_%d.world", mapNumber));
                var map = new WorldMap(mapURL);
                yield createModernWorld(map);
            }
        };
        setWorldAndCreatePopulation(world);
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
    }

    @Override
    public int intermissionNumberAfterLevel(int levelNumber) {
        return 0;
    }

    private World createModernWorld(WorldMap map) {
        var modernWorld = new World(map);
        int topLeftX = map.numCols() / 2 - 4;
        int topLeftY = map.numRows() / 2 - 3;
        House house = House.createArcadeHouse(v2i(topLeftX, topLeftY));
        house.setPacPositionFromMap(map);
        house.setGhostPositionsFromMap(map);
        modernWorld.addHouse(house);
        modernWorld.setBonusPosition(halfTileRightOf(13, 20)); // TODO get position from map?
        return modernWorld;
    }
}