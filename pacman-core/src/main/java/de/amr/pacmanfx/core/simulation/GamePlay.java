/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.simulation;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Bonus;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;

public interface GamePlay {

    // Game start

    void init(GameModel model);

    void resetForNewGame(GameModel model);

    void prepareLevelForPlaying(GameLevel level);

    // Level building and level start

    GameLevel createLevel(GameModel model, int levelNumber, boolean demoLevel);

    GameLevel buildDemoLevel(GameContext playContext);

    void buildNormalLevel(GameContext playContext, int levelNumber);

    boolean isDemoLevelRunning(GameModel model);

    boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    void startLevel(GameContext playContext);

    void startNextLevel(GameContext playContext);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    // Playing level

    HuntingStepResult hunt(GameContext playContext);

    void onEatPellet(GameContext playContext, Vector2i tile);

    void onEatEnergizer(GameContext playContext, Vector2i tile);

    void onEatBonus(GameContext playContext, Bonus bonus);

    void onEatGhost(GameContext playContext, Ghost eatenGhost);

    void activateNextBonus(GameContext playContext);

    void startPacPowerMode(GameContext playContext, Pac pac);

    void updatePacPowerMode(GameContext playContext, Pac pac);

    void onLevelCompleted(GameLevel level);

    // Scoring

    void scorePoints(GameContext playContext, int points, int levelNumber);

    void updateHighScore(GameContext playContext);
}
