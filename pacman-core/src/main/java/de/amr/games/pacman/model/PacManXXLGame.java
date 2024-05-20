/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RuleBasedPacSteering;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.model.world.WorldMap;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * Extension of Arcade Pac-Man with 8 additional mazes (thanks to the one and only Sean Williams!).
 */
public class PacManXXLGame extends PacManGame{

    public PacManXXLGame() {
        initialLives = 3;
        highScoreFileName = "highscore-pacman_xxl.xml";
        reset();
        Logger.info("Game variant {} initialized.", this);
    }

    @Override
    public GameVariant variant() {
        return GameVariant.PACMAN_XXL;
    }

    @Override
    void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        switch (levelNumber) {
            case 1 -> setWorldAndCreatePopulation(createPacManWorld());
            case 2, 3, 4, 5, 6, 7, 8, 9 -> {
                var map = loadMap(String.format("/maps/masonic/masonic_%d.world", levelNumber - 1));
                setWorldAndCreatePopulation(createModernWorld(map));
            }
            default -> {
                var map = loadMap(String.format("/maps/masonic/masonic_%d.world", randomInt(2, 9)));
                setWorldAndCreatePopulation(createModernWorld(map));
            }
        }
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
    }

    @Override
    public int intermissionNumberAfterLevel(int levelNumber) {
        return 0;
    }

    // uses Masonic map, no special tiles where hunting ghosts cannot pass
    World createModernWorld(WorldMap map) {
        var world = new World(map);
        world.setHouse(createArcadeHouse());
        world.house().setTopLeftTile(v2i(10, 15));
        world.setPacPosition(halfTileRightOf(13, 26));
        world.setGhostPositions(new Vector2f[] {
            halfTileRightOf(13, 14), // red ghost
            halfTileRightOf(13, 17), // pink ghost
            halfTileRightOf(11, 17), // cyan ghost
            halfTileRightOf(15, 17)  // orange ghost
        });
        world.setGhostDirections(new Direction[] {Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP});
        world.setGhostScatterTiles(new Vector2i[] {
            v2i(25,  0), // near right-upper corner
            v2i( 2,  0), // near left-upper corner
            v2i(27, 34), // near right-lower corner
            v2i( 0, 34)  // near left-lower corner
        });
        world.setBonusPosition(halfTileRightOf(13, 20));
        return world;
    }
}