/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.halfTileRightOf;
import static de.amr.games.pacman.lib.Globals.randomInt;
import static de.amr.games.pacman.model.GameModel.checkLevelNumber;

/**
 * Extension of Arcade Pac-Man with 8 additional mazes (thanks to the one and only Sean Williams!).
 */
public class PacManXXLGame extends PacManGame {

    private final List<WorldMap> customMaps = new ArrayList<>();

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
        loadCustomMaps(CUSTOM_MAP_DIR);
    }

    @Override
    public void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        var world = switch (levelNumber) {
            case 1 -> customMaps.isEmpty() ? createPacManWorld() : createModernWorld(customMaps.getFirst());
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

    private static World createModernWorld(WorldMap map) {
        var modernWorld = new World(map);
        int houseTopRow = map.numRows() / 2 - 3;
        int houseTopCol = map.numCols() / 2 - 4;
        modernWorld.addHouse(House.createArcadeHouse(houseTopRow, houseTopCol));
        modernWorld.setGhostDirections(new Direction[] {Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP}); // TODO
        modernWorld.setBonusPosition(halfTileRightOf(13, 20)); // TODO get position from map?
        return modernWorld;
    }

    private void loadCustomMaps(File mapDir) {
        if (!mapDir.isDirectory()) {
            Logger.error("Specified map directory path '{}' does not point to a directory", mapDir);
            return;
        }
        Logger.info("Searching for custom map files in folder {}", mapDir);
        customMaps.clear();
        var mapFiles = mapDir.listFiles((dir, name) -> name.endsWith(".world"));
        if (mapFiles != null) {
            for (var mapFile : mapFiles) {
                customMaps.add(new WorldMap(mapFile));
                Logger.info("Found custom map file: " + mapFile);
            }
            if (customMaps.isEmpty()) {
                Logger.info("No custom maps found");
            } else {
                Logger.info("{} custom map(s) loaded", customMaps.size());
            }
        } else {
            Logger.error("Could not access custom map folder {}", mapDir);
        }
    }
}