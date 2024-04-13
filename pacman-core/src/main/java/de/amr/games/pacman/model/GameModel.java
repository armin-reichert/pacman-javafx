/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Score;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Common interface of all game variants.
 *
 * @author Armin Reichert
 */
public interface GameModel {

    // Ghost IDs
    byte RED_GHOST    = 0;
    byte PINK_GHOST   = 1;
    byte CYAN_GHOST   = 2;
    byte ORANGE_GHOST = 3;

    Direction[] GHOST_DIRECTIONS_ON_START = {
        Direction.LEFT,
        Direction.DOWN,
        Direction.UP,
        Direction.UP
    };

    Vector2f[] GHOST_POSITIONS_ON_START = {
        ArcadeWorld.HOUSE_ENTRY_POSITION,
        ArcadeWorld.HOUSE_MIDDLE_SEAT,
        ArcadeWorld.HOUSE_LEFT_SEAT,
        ArcadeWorld.HOUSE_RIGHT_SEAT
    };

    Vector2f[] GHOST_REVIVAL_POSITIONS = {
        ArcadeWorld.HOUSE_MIDDLE_SEAT,
        ArcadeWorld.HOUSE_MIDDLE_SEAT,
        ArcadeWorld.HOUSE_LEFT_SEAT,
        ArcadeWorld.HOUSE_RIGHT_SEAT
    };

    /** Game loop frequency, ticks per second. */
    short FPS = 60;

    short PAC_POWER_FADING_TICKS = 2 * FPS; // unsure
    short BONUS_POINTS_SHOWN_TICKS = 2 * FPS; // unsure

    // after eating, Pac-Man must rest a bit
    byte RESTING_TICKS_PELLET = 1;
    byte RESTING_TICKS_ENERGIZER = 3;

    /** Base speed of creatures in pixels per second. */
    short PPS_AT_100_PERCENT = 75;
    short PPS_GHOST_INHOUSE = 30; // correct?
    short PPS_GHOST_RETURNING_HOME = 120; // correct?

    short POINTS_PELLET = 10;
    short POINTS_ENERGIZER = 50;
    short POINTS_ALL_GHOSTS_KILLED_IN_LEVEL = 12_000;
    short EXTRA_LIFE_SCORE = 10_000;

    byte LEVEL_COUNTER_MAX_SYMBOLS = 7;

    /**
     * @return Readable name (Pac-Man, Ms. Pac-Man)
     */
    String pacName();

    /**
     * @param id ghost ID
     * @return Readable ghost name (Blinky, Pinky, Inky, Clyde/Sue)
     */
    String ghostName(byte id);

    /**
     * @param level game level
     * @return {@code true} if bonus score is reached for this level
     */
    boolean isBonusReached(GameLevel level);

    /**
     * @param levelNumber game level number
     * @return next bonus symbol for this level
     */
    byte nextBonusSymbol(int levelNumber);

    /**
     * @param symbol bonus symbol
     * @return points earned when eating this bonus
     */
    int bonusValue(byte symbol);

    /**
     * @param world game world
     * @param bonus current bonus
     * @param bonusIndex next bonus index in current level (0 or 1)
     * @param symbol next bonus symbol
     * @return optional bonus to appear next
     */
    Optional<Bonus> createNextBonus(World world, Bonus bonus, int bonusIndex, byte symbol);

    /**
     * @param levelNumber game level number
     * @return ticks per hunting phase (scatter1, chasing1, ..., scatter4, chasing4)
     */
    int[] huntingDurations(int levelNumber);

    /**
     * @return File where high score is saved
     */
    File highScoreFile();

    /**
     * Creates and initializes new game level with the given number.
     *
     * @param levelNumber level number (starting at 1)
     */
    void createAndStartLevel(int levelNumber);

    /**
     * Creates and starts the demo game level ("attract mode"). Behavior of the ghosts is different from the original
     * Arcade game because they do not follow a predetermined path but change their direction randomly when frightened.
     * In Pac-Man variant, Pac-Man at least follows the same path as in the Arcade game, but in Ms. Pac-Man game, she
     * does not behave as in the Arcade game but hunts the ghosts using some goal-driven algorithm.
     */
    void createAndStartDemoLevel();

    /**
     * @param playing Sets playing state for current game
     */
    void setPlaying(boolean playing);

    /**
     * @return Tells if game is playing
     */
    boolean isPlaying();

    /**
     * Resets the game and deletes the current level. Credit, immunity and scores remain unchanged.
     */
    void reset();

    /**
     * @return current game level if present
     */
    Optional<GameLevel> level();

    /**
     * @return Number of lives at game start
     */
    short initialLives();

    /**
     * Sets number of lives at game start.
     * @param lives number of lives
     */
    void setInitialLives(int lives);

    /**
     * @return Total number of lives remaining
     */
    int lives();

    /**
     * Adds given number of lives.
     * @param lives number of lives
     */
    void addLives(int lives);

    /**
     * Removes one life.
     */
    void loseLife();

    /**
     * @return List of symbols representing levels completed so far.
     */
    List<Byte> levelCounter();

    /**
     * Adds symbol to level counter list.
     * @param symbol bonus symbol
     */
    void addSymbolToLevelCounter(byte symbol);

    /**
     * Defines the hunting behaviour of the given ghost in the given level.
     * @param ghost a ghost
     * @param level game level
     */
    void huntingBehaviour(Ghost ghost, GameLevel level);

    /**
     * @return the score of the current game
     */
    Score score();

    /**
     * @return the high score
     */
    Score highScore();

    default void loadHighScore() {
        highScore().loadFromFile(highScoreFile());
    }

    /**
     * Scores given amount of points
     * @param levelNumber current level number
     * @param points points to score
     */
    void scorePoints(int levelNumber, int points);

    /**
     * Updates the high score.
     */
    void updateHighScore();

    /**
     * Adds a game event listener (for example a user interface) to this game
     * @param listener game event listener
     */
    void addGameEventListener(GameEventListener listener);

    /**
     * Published a game event of the given type
     * @param type game event type
     */
    void publishGameEvent(GameEventType type);

    /**
     * Published a game event of the given type which is related to the given tile
     * @param type game event type
     */
    void publishGameEvent(GameEventType type, Vector2i tile);

    /**
     * Publishes the given game event.
     * @param event game event
     */
    void publishGameEvent(GameEvent event);
}