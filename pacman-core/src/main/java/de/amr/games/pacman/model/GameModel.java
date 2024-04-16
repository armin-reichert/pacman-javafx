/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    byte[][] LEVEL_DATA = {
        /* 1*/ { 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5, 0},
        /* 2*/ { 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5, 1},
        /* 3*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5, 0},
        /* 4*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5, 0},
        /* 5*/ {100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5, 2},
        /* 6*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5, 0},
        /* 7*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0},
        /* 8*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0},
        /* 9*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3, 3},
        /*10*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5, 0},
        /*11*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5, 0},
        /*12*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 0},
        /*13*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 3},
        /*14*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5, 0},
        /*15*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
        /*16*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
        /*17*/ {100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0, 3},
        /*18*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
        /*19*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
        /*20*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
        /*21*/ { 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
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

    Pac pac();


    /**
     * @param id ghost ID, one of {@link GameModel#RED_GHOST}, {@link GameModel#PINK_GHOST},
     *           {@value GameModel#CYAN_GHOST}, {@link GameModel#ORANGE_GHOST}
     * @return the ghost with the given ID
     */
    Ghost ghost(byte id);

    /**
     * @param states states specifying which ghosts are returned
     * @return all ghosts which are in any of the given states or all ghosts, if no states are specified
     */
    public Stream<Ghost> ghosts(GhostState... states);

    /**
     * @return {@code true} if bonus score is reached for this level
     */
    boolean isBonusReached();

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
     * @return File where high score is saved
     */
    File highScoreFile();

    /**
     * Creates and initializes new game level with the given number.
     * <p>
     * If {@code demoLevel=true}, creates and starts the demo game level ("attract mode"). Behavior of the ghosts is different from the original
     * Arcade game because they do not follow a predetermined path but change their direction randomly when frightened.
     * In Pac-Man variant, Pac-Man at least follows the same path as in the Arcade game, but in Ms. Pac-Man game, she
     * does not behave as in the Arcade game but hunts the ghosts using some goal-driven algorithm.
     *
     * @param levelNumber level number (starting at 1)
     * @param demoLevel if demo level is created and started
     */
    void createAndStartLevel(int levelNumber, boolean demoLevel);

    /**
     * Pac-Man and the ghosts are placed at their initial positions and locked. The bonus, Pac-Man power timer and
     * energizer pulse are reset too.
     */
    void letsGetReadyToRumble();

    void onPacDying();

    /**
     * Called on bonus achievement (public access for unit tests and level test).
     *
     * @param index bonus index (0 or 1).
     */
    void onBonusReached(int index);

    Optional<Bonus> bonus();

    void killGhosts(List<Ghost> prey);

    void onLevelCompleted();

    GameState doHuntingStep();

    TickTimer huntingTimer();

    void startHuntingPhase(int index);

    byte huntingPhaseIndex();

    Optional<Integer> scatterPhase();

    Optional<Integer> chasingPhase();

    String currentHuntingPhaseName();

    void doLevelTestStep(TickTimer timer, int lastTestedLevel);

    /**
     * @return Blinky's "cruise elroy" state. Values: <code>0, 1, 2, -1, -2</code>. (0=off, negative=disabled).
     */
    byte cruiseElroyState();

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

    GameLevel level();

    World world();

    Pulse blinking();

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