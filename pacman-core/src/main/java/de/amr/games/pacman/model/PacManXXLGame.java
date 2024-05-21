/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.model.world.WorldMap;
import org.tinylog.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * Extension of Arcade Pac-Man with 8 additional mazes (thanks to the one and only Sean Williams!).
 */
public class PacManXXLGame extends PacManGame{

    private final List<WorldMap> customMaps = new ArrayList<>();

    public PacManXXLGame() {
        initialLives = 3;
        highScoreFileName = "highscore-pacman_xxl.xml";
        reset();
        Logger.info("Game variant {} initialized.", this);
        try {
            loadCustomMaps();
        } catch (IOException x) {
            Logger.error("Loading custom maps failed");
            Logger.error(x);
        }
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
                var path = String.format("/maps/masonic/masonic_%d.world", levelNumber - 1);
                var map = loadMap(path);
                setWorldAndCreatePopulation(createModernWorld(map));
            }
            default -> {
                var path = String.format("/maps/masonic/masonic_%d.world", randomInt(2, 9));
                var map = loadMap(path);
                setWorldAndCreatePopulation(createModernWorld(map));
            }
        }
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
    }

    @Override
    void buildDemoLevel() {
        levelNumber = 1;
        if (customMaps.isEmpty()) {
            setWorldAndCreatePopulation(createPacManWorld());
        } else {
            setWorldAndCreatePopulation(createModernWorld(customMaps.getFirst()));
        }
        pac.setName("Pac-Man");
        pac.setAutopilot(world.getDemoLevelRoute().isEmpty()
            ? new RuleBasedPacSteering(this)
            : new RouteBasedSteering(world.getDemoLevelRoute()));
        pac.setUseAutopilot(true);
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

    void loadCustomMaps() throws IOException {
        var dir = new File(System.getProperty("user.home"), ".pacmanfx/maps");
        if (dir.isDirectory()) {
            var filterWorldFiles = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".world");
                }
            };
            Logger.info("Searching for custom map files in folder " + dir);
            var mapFiles = dir.listFiles(filterWorldFiles);
            if (mapFiles != null) {
                for (var mapFile : mapFiles) {
                    Logger.info("Found map file: " + mapFile);
                    URL url = mapFile.toURI().toURL();
                    customMaps.add(new WorldMap(url));
                }
            }
        }
    }
}