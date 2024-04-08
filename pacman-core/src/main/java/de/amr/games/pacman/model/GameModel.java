/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Score;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * Common interface of all game variants.
 *
 * @author Armin Reichert
 */
public interface GameModel {

    byte RED_GHOST    = 0;
    byte PINK_GHOST   = 1;
    byte CYAN_GHOST   = 2;
    byte ORANGE_GHOST = 3;

    /** Game loop frequency. */
    short FPS = 60;

    short PAC_POWER_FADING_TICKS = 2 * FPS; // unsure
    short BONUS_POINTS_SHOWN_TICKS = 2 * FPS; // unsure
    /** Base speed of creatures (Pac-Man, ghosts, moving bonus). */
    short PPS_AT_100_PERCENT = 75;
    short PPS_GHOST_INHOUSE = 38; // correct?
    short PPS_GHOST_RETURNING_HOME = 150; // correct?
    byte RESTING_TICKS_NORMAL_PELLET = 1;
    byte RESTING_TICKS_ENERGIZER = 3;
    short POINTS_NORMAL_PELLET = 10;
    short POINTS_ENERGIZER = 50;
    short POINTS_ALL_GHOSTS_KILLED_IN_LEVEL = 12_000;
    short EXTRA_LIFE_SCORE = 10_000;
    byte LEVEL_COUNTER_MAX_SYMBOLS = 7;

    String pacName();

    String ghostName(byte id);

    boolean isBonusReached(GameLevel level);

    byte nextBonusSymbol(int levelNumber);

    int bonusValue(byte symbol);

    Optional<Bonus> createNextBonus(World world, Bonus bonus, int bonusIndex, byte symbol);

    int[] huntingDurations(int levelNumber);

    File highScoreFile();

    /**
     * Starts new game level with the given number.
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

    void setPlaying(boolean playing);

    boolean isPlaying();

    /**
     * Resets the game and deletes the current level. Credit, immunity and scores remain unchanged.
     */
    void reset();

    void setLevel(GameLevel level);

    Optional<GameLevel> level();

    short initialLives();

    void setInitialLives(short initialLives);

    int lives();

    void addLives(short lives);

    void loseLife();

    List<Byte> levelCounter();

    void addSymbolToLevelCounter(byte symbol);

    Score score();

    Score highScore();

    void updateHighScore();

    void addGameEventListener(GameEventListener listener);

    void publishGameEvent(GameEventType type);

    void publishGameEvent(GameEventType type, Vector2i tile);

    void publishGameEvent(GameEvent event);
}