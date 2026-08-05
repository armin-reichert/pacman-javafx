/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.bonus.Bonus;
import de.amr.pacmanfx.core.entities.ghost.Ghost;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;

public interface GamePlay {

    // Game start

    void init(GameContext gameContext);

    void resetForNewGame(GameContext gameContext);

    void prepareLevelForPlaying(GameContext gameContext);

    // Level building and level start

    GameLevel createLevel(GameContext gameContext, int levelNumber, boolean demoLevel);

    GameLevel buildDemoLevel(GameContext gameContext);

    void buildNormalLevel(GameContext gameContext, int levelNumber, int numLives);

    boolean isDemoLevelRunning(GameContext gameContext);

    boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    void startLevel(GameContext gameContext);

    void startNextLevel(GameContext gameContext);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    // Playing level

    void hunt(GameContext gameContext, GameLevel level);

    void onEatPellet(GameContext gameContext, GameLevel level, Vector2i tile);

    void onEatEnergizer(GameContext gameContext, GameLevel level, Vector2i tile);

    void onEatBonus(GameContext gameContext, GameLevel level, Bonus bonus);

    void onEatGhost(GameContext gameContext, GameLevel level, Ghost eatenGhost);

    void activateNextBonus(GameContext gameContext, GameLevel level);

    void onLevelCompleted(GameContext gameContext, GameLevel level);

    // Scoring

    void scorePoints(GameContext gameContext, int points, int levelNumber);

    void updateHighScore(GameContext gameContext);
}
