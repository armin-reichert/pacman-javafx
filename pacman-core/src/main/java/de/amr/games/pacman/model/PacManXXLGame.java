/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.RuleBasedPacSteering;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.net.URL;

import static de.amr.games.pacman.lib.Globals.*;

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
    public World createWorld(int mapNumber) {
        World world;
        if (mapNumber == 1) {
            world = createPacManWorld();
        } else { // use one of Sean William's maps
            var path = String.format("/maps/masonic/masonic_%d.world", mapNumber - 1);
            try {
                URL url = getClass().getResource(path);
                world = createArcadeWorld(url);
                Logger.info("Created world from map '{}'", path + ".world");
            } catch (Exception x) {
                throw new IllegalArgumentException(String.format("Could not create world map from path '%s'", path));
            }
        }
        world.setBonusPosition(halfTileRightOf(13, 20));
        return world;
    }

    @Override
    void buildRegularLevel(int levelNumber) {
        this.levelNumber = checkLevelNumber(levelNumber);
        if (levelNumber == 1) {
            mapNumber = 1; // Pac-Man map
        } else if (levelNumber <= 9) {
            mapNumber = levelNumber; // Masonic map 1-8
        }
        else {
            mapNumber = randomInt(2, 9); // random Masonic map
        }
        populateLevel(createWorld(mapNumber));
        pac.setName("Pac-Man");
        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUseAutopilot(false);
    }

    @Override
    public int intermissionNumberAfterLevel(int levelNumber) {
        return 0;
    }
}