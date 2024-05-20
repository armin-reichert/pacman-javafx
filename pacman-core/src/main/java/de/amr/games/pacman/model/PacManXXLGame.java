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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.UP;
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
        var map = switch (levelNumber) {
            case 1 -> loadMap("/maps/pacman.world");
            case 2, 3, 4, 5, 6, 7, 8, 9 -> loadMap(String.format("/maps/masonic/masonic_%d.world", levelNumber - 1));
            default -> loadMap(String.format("/maps/masonic/masonic_%d.world", randomInt(2, 9)));
        };
        var world = levelNumber == 1 ? createClassicWorld(map) : createModernWorld(map);
        populateLevel(world);
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
    }

    @Override
    public int intermissionNumberAfterLevel(int levelNumber) {
        return 0;
    }

    World createClassicWorld(WorldMap map) {
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
        world.setDemoLevelRoute(List.of(PACMAN_ARCADE_MAP_DEMO_LEVEL_ROUTE));
        List<Direction> up = List.of(UP);
        Map<Vector2i, List<Direction>> fp = new HashMap<>();
        Stream.of(v2i(12, 14), v2i(15, 14), v2i(12, 26), v2i(15, 26))
            .forEach(tile -> fp.put(tile, up));
        world.setForbiddenPassages(fp);
        return world;
    }

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
        return world;
    }

}