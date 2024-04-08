/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Score;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.world.World;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Game models/variants that can be played.
 *
 * @author Armin Reichert
 */
public interface GameModelInterface {

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
}