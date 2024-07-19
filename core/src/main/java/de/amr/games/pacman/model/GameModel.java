/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Common interface of all game models.
 *
 * @author Armin Reichert
 */
public interface GameModel {

    static byte checkGhostID(byte id) {
        if (id < 0 || id > 3) {
            throw GameException.illegalGhostID(id);
        }
        return id;
    }

    static int checkLevelNumber(int number) {
        if (number < 1) {
            throw GameException.illegalLevelNumber(number);
        }
        return number;
    }

    File GAME_DIR = new File(System.getProperty("user.home"), ".pacmanfx");
    File CUSTOM_MAP_DIR = new File(GAME_DIR, "maps");

    byte ARCADE_MAP_TILES_X = 28;
    byte ARCADE_MAP_TILES_Y = 36;
    int  ARCADE_MAP_SIZE_X = ARCADE_MAP_TILES_X * TS;
    int  ARCADE_MAP_SIZE_Y = ARCADE_MAP_TILES_Y * TS;

    // Ghost IDs
    byte RED_GHOST = 0, PINK_GHOST = 1, CYAN_GHOST = 2, ORANGE_GHOST = 3;

    /** Game loop frequency, ticks per second. */
    float FPS = 60;

    /** Duration of one tick (in seconds). */
    float SEC_PER_TICK = 1f / FPS;

    /** Movement speed in pixel/sec. */
    float PPS_AT_100_PERCENT = 73.9f; //TODO this should be 75 but that doesn't work yet

    void init();

    default GameController controller() {
        return GameController.it();
    }

    GameVariant variant();

    SimulationStepEventLog eventLog();

    void clearEventLog();

    /**
     * @return Pac-Man or Ms. Pac-Man
     */
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
    Stream<Ghost> ghosts(GhostState... states);

    /**
     * Creates and initializes new game level with the given number.
     * <p>
     * If {@code demoLevel=true}, creates and starts the demo game level ("attract mode").
     * Behavior of the ghosts is different from the original Arcade game because they do not follow a
     * predetermined path but change their direction randomly when frightened.
     * In Pac-Man variant, Pac-Man at least follows the same path as in the Arcade game, but in Ms. Pac-Man game, she
     * does not behave as in the Arcade game but hunts the ghosts using some goal-driven algorithm.
     *
     * @param levelNumber level number (starting at 1)
     */
    void createLevel(int levelNumber);

    /**
     * Creates and initializes a demo level.
     */
    void createDemoLevel();

    int intermissionNumber(int levelNumber);

    void activateNextBonus();

    /**
     * At this point, the animations of Pac-Man and the ghosts must have been created!
     */
    void startLevel();

    /**
     * @return Current level number (starting at 1, 0 means no level yet)
     */
    int levelNumber();

    Optional<GameLevel> level();

    /**
     * @return if the current level is a demo level (attract mode)
     */
    boolean isDemoLevel();

    void removeWorld();

    /**
     * Pac-Man and the ghosts are placed at their initial positions and locked. The bonus, Pac-Man power timer and
     * energizer pulse are reset too.
     */
    void letsGetReadyToRumble();

    void makeGuysVisible(boolean visible);

    boolean isLevelComplete();

    void onLevelCompleted();

    boolean isPacManKilled();

    void onPacDying();

    void killGhost(Ghost prey);

    List<Ghost> victims();

    void doHuntingStep();

    boolean areGhostsKilled();

    TickTimer huntingTimer();

    TickTimer powerTimer();

    boolean isPowerFading();

    boolean isPowerFadingStarting();

    /**
     * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
     * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
     * ghosts attack Pac-Man.
     *
     * @param phaseIndex hunting phase index (0..7)
     */
    void startHuntingPhase(int phaseIndex);

    int huntingPhaseIndex();

    default boolean isScatterPhase(int phaseIndex) {
        return isEven(phaseIndex);
    }

    default boolean isChasingPhase(int phaseIndex) {
        return isOdd(phaseIndex);
    }

    /**
     * @return (optional) index of current scattering phase <code>(0-3)</code>
     */
    Optional<Integer> scatterPhase();

    /**
     * @return (optional) index of current chasing phase <code>(0-3)</code>
     */
    Optional<Integer> chasingPhase();

    /**
     * @return Blinky's "cruise elroy" state. Values: <code>0, 1, 2, -1, -2</code>. (0=off, negative=disabled).
     */
    byte cruiseElroyState();

    /**
     * @param enabled if the overflow bug should be enabled
     * @see <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">Don Hodges' explanation of overflow bug</a>
     */
    void setOverflowBug(boolean enabled);

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

    GameWorld world();

    Pulse blinking();

    /**
     * @return Number of lives at game start
     */
    int initialLives();

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
     * @param lives number of lives
     */
    void addLives(int lives);

    void loseLife();

    /**
     * @return List of symbols representing levels completed so far.
     */
    List<Byte> levelCounter();

    /**
     * @return the score of the current game
     */
    Score score();

    /**
     * @return the high score
     */
    Score highScore();

    /**
     * @param points points to score
     */
    void scorePoints(int points);

    void loadHighScore();

    void updateHighScore();

    Optional<Bonus> bonus();

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
     * @param tile tile associated with event
     */
    void publishGameEvent(GameEventType type, Vector2i tile);

    /**
     * Publishes the given game event.
     * @param event game event
     */
    void publishGameEvent(GameEvent event);
}