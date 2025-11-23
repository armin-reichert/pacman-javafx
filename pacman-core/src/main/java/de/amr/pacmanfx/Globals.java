/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.controller.GameController;
import de.amr.pacmanfx.lib.math.Vector2i;

public interface Globals {

    /**
     * The red ghost's character is aptly described as that of a shadow and is best-known as “Blinky”.
     * In Japan, his character is represented by the word oikake, which means “to run down or pursue”.
     * Blinky seems to always be the first of the ghosts to track Pac-Man down in the maze.
     * He is by far the most aggressive of the four and will doggedly pursue Pac-Man once behind him.
      */
    byte RED_GHOST_SHADOW = 0;

    /**
     * Nicknamed “Pinky”, the pink ghost's character is described as one who is speedy.
     * In Japan, he is characterized as machibuse, meaning “to perform an ambush”, perhaps because Pinky always seems
     * to be able to get ahead of you and cut you off when you least expect it. He always moves at the same speed
     * as Inky and Clyde, however, which suggests speedy is a poor translation of the more appropriate machibuse.
     * Pinky and Blinky often seem to be working in concert to box Pac-Man in, leaving him with nowhere to run.
     */
    byte PINK_GHOST_SPEEDY = 1;

    /**
     * The light-blue ghost is nicknamed “Inky” and his character is described as one who is bashful.
     * In Japan, he is portrayed as kimagure, meaning “a fickle, moody, or uneven temper”.
     * Perhaps not surprisingly, Inky is the least predictable of the ghosts. Sometimes he chases Pac-Man aggressively
     * like Blinky; other times he jumps ahead of Pac-Man as Pinky would. He might even wander off like Clyde on occasion!
     * In fact, Inky may be the most dangerous ghost of all due to his erratic behavior.
     * Bashful is not a very good translation of kimagure, and misleads the player to assume Inky will shy away
     * from Pac-Man when he gets close which is not always the case.
     */
    byte CYAN_GHOST_BASHFUL = 2;

    /**
     * The orange ghost is nicknamed “Clyde” and is characterized as one who is pokey.
     * In Japan, his character is described as otoboke, meaning “pretending ignorance”, and his nickname is “Guzuta”,
     * meaning “one who lags behind”. In reality, Clyde moves at the same speed as Inky and Pinky so his character
     * description is a bit misleading. Clyde is the last ghost to leave the pen and tends to separate himself
     * from the other ghosts by shying away from Pac-Man and doing his own thing when he isn't patrolling his corner
     * of the maze. Although not nearly as dangerous as the other three ghosts, his behavior can seem unpredictable
     * at times and should still be considered a threat.
     */
    byte ORANGE_GHOST_POKEY = 3;

    byte NUM_TICKS_PER_SEC = 60;

    /** Tile size: 8px. */
    byte TS = 8;

    /** Half tile size: 4px. */
    byte HTS = 4;

    /**
     * @param numTiles number of tiles
     * @return number of pixels corresponding to given number of tiles
     */
    static float TS(float numTiles) { return numTiles * TS; }

    /**
     * Arcade maps have a size of 28 cols and 36 rows (including the empty rows over and under the maze).
     */
    Vector2i ARCADE_MAP_SIZE_IN_TILES = new Vector2i(28, 36);

    /**
     * Arcade maps have a size of 28x36 tiles (28 cols, 36 rows, including the empty rows over and under the maze).
     * The tile size is 8px which gives a map size of 224x288px.
     */
    Vector2i ARCADE_MAP_SIZE_IN_PIXELS = new Vector2i(224, 288); // 28x36 tiles

    /**
     * @return the global object which provides access to different other global objects
     */
    static GameContext theGameContext() {
        if (GameController.THE_GAME_CONTROLLER == null) {
            GameController.THE_GAME_CONTROLLER = new GameController();
        }
        return GameController.THE_GAME_CONTROLLER;
    }
}