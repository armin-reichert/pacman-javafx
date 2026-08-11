/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;

import java.io.File;

public interface GamePlay {

    // Game start

    void onSessionStart(GameContext game);

    void prepareLevelForPlaying(GameContext game);

    // Level building and level start

    GameLevel createLevel(GameContext game, int levelNumber, boolean demoLevel);

    GameLevel buildDemoLevel(GameContext game);

    void buildNormalLevel(GameContext game, int levelNumber, int numLives);

    boolean isDemoLevelRunning(GameContext game);

    boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    void startLevel(GameContext game);

    void startNextLevel(GameContext game);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    // Playing level

    void hunt(GameContext game, GameLevel level);

    void updateEntities(GameContext game, GameLevel level);

    void onEatPellet(GameContext game, GameLevel level, Vector2i tile);

    void onEatEnergizer(GameContext game, GameLevel level, Vector2i tile);

    void onEatBonus(GameContext game, GameLevel level, Bonus bonus);

    void onEatGhost(GameContext game, GameLevel level, Ghost eatenGhost);

    void activateNextBonus(GameContext game, GameLevel level);

    void onLevelCompleted(GameContext game, GameLevel level);

    // Scoring

    void scorePoints(GameContext game, int points, int levelNumber);
}
