/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.lib.WorldMap;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.checkLevelNumber;

/**
 * Extension of Arcade Pac-Man with 8 additional mazes (thanks to the one and only Sean Williams!).
 */
public class PacManXXLGame extends PacManGame {

    private final List<WorldMap> customMaps = new ArrayList<>();

    @Override
    public void init() {
        initialLives = 3;
        highScoreFileName = "highscore-pacman_xxl.xml";
        Logger.info("Game variant {} initialized.", variant());
    }

    @Override
    public GameVariant variant() {
        return GameVariant.PACMAN_XXL;
    }

    @Override
    public void reset() {
        super.reset();
        try {
            loadCustomMaps();
            Logger.info("{} custom maps loaded", customMaps.size());
        } catch (IOException x) {
            Logger.error("Loading custom maps failed");
            Logger.error(x);
        }
    }

    @Override
    void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        switch (levelNumber) {
            case 1 -> {
                if (customMaps.isEmpty()) {
                    setWorldAndCreatePopulation(createPacManWorld());
                } else {
                    // entry point for testing custom maps
                    setWorldAndCreatePopulation(createModernWorld(customMaps.getFirst()));
                }
            }
            case 2, 3, 4, 5, 6, 7, 8, 9 -> {
                var path = String.format("/maps/masonic/masonic_%d.world", levelNumber - 1);
                setWorldAndCreatePopulation(createModernWorld(loadMap(path)));
            }
            default -> {
                var path = String.format("/maps/masonic/masonic_%d.world", randomInt(1, 9));
                setWorldAndCreatePopulation(createModernWorld(loadMap(path)));
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

    World createModernWorld(WorldMap map) {
        var world = new World(map);
        world.addHouse(createArcadeHouse(), v2i(10, 15)); //TODO create house from map?
        world.setGhostDirections(new Direction[] {Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP}); // TODO
        world.setBonusPosition(halfTileRightOf(13, 20)); // TODO get position from map?
        return world;
    }

    void loadCustomMaps() throws IOException {
        customMaps.clear();
        var mapDir = CUSTOM_MAP_DIR;
        if (mapDir.isDirectory()) {
            Logger.info("Searching for custom map files in folder " + mapDir);
            var mapFiles = mapDir.listFiles((dir, name) -> name.endsWith(".world"));
            if (mapFiles != null) {
                for (var mapFile : mapFiles) {
                    URL url = mapFile.toURI().toURL();
                    customMaps.add(new WorldMap(url));
                    Logger.info("Found custom map file: " + mapFile);
                }
            } else {
                Logger.error("Could not access custom map folder {}", mapDir);
            }
        }
    }
}