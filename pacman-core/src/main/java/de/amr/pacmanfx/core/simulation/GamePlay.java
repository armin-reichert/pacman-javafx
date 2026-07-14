/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.simulation;

import de.amr.basics.math.Vector2i;
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

    GameLevel buildDemoLevel(GamePlayContext playContext);

    void buildNormalLevel(GamePlayContext playContext, int levelNumber);

    boolean isDemoLevelRunning(GameModel model);

    boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    void startLevel(GamePlayContext playContext);

    void startNextLevel(GamePlayContext playContext);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    // Playing level

    HuntingStepResult hunt(GamePlayContext playContext);

    void onEatPellet(GamePlayContext playContext, Vector2i tile);

    void onEatEnergizer(GamePlayContext playContext, Vector2i tile);

    void onEatBonus(GamePlayContext playContext, Bonus bonus);

    void onEatGhost(GamePlayContext playContext, Ghost eatenGhost);

    void activateNextBonus(GamePlayContext playContext);

    void startPacPowerMode(GamePlayContext playContext, Pac pac);

    void updatePacPowerMode(GamePlayContext playContext, Pac pac);

    void onLevelCompleted(GameLevel level);

    // Scoring

    void scorePoints(GamePlayContext playContext, int points, int levelNumber);

    void updateHighScore(GamePlayContext playContext);
}
