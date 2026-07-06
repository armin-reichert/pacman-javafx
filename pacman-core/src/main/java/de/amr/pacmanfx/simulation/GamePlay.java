/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;

public interface GamePlay {

    // Game start

    void init(GameModel model);

    void resetForNewGame(GameModel model);

    void prepareLevelForPlaying(GameLevel level);

    // Level building and level start

    GameLevel buildDemoLevel(GamePlayContext playContext);

    boolean isDemoLevelRunning(GameModel model);

    boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    void buildNormalLevel(GamePlayContext playContext, int levelNumber);

    void startLevel(GamePlayContext playContext);

    void startNextLevel(GamePlayContext playContext);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    // Playing level

    HuntingStepResult hunt(GamePlayContext playContext);

    void onEatPellet(GamePlayContext playContext, Vector2i tile);

    void onEatEnergizer(GamePlayContext playContext, Vector2i tile);

    void onEatBonus(GamePlayContext playContext, Bonus bonus);

    void onEatGhost(GamePlayContext playContext, Ghost eatenGhost);

    void activateNextBonus(GameEventManager eventManager, GameLevel level);

    void startPacPowerMode(GameEventManager eventManager, GameLevel level, Pac pac);

    void updatePacPowerMode(GameEventManager eventManager, GameLevel level, Pac pac);

    void onLevelCompleted(GameLevel level);

    // Scoring

    void scorePoints(GameEventManager eventManager, GameModel model, int points, int levelNumber);

    void updateHighScore(GameEventManager eventManager, GameModel model);
}
