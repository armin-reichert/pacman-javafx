/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
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

    //TODO check if dependency from context can be removed
    boolean canStartNewGame(GameContext context);

    void resetForNewGame(GameModel model);

    void prepareLevelForPlaying(GameLevel level);

    // Level building and level start

    GameLevel buildDemoLevel(GameEventManager eventManager, GameModel model);

    boolean isDemoLevelRunning(GameContext context);

    boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    void buildNormalLevel(GameEventManager eventManager, GameModel model, int levelNumber);

    void startLevel(GameEventManager eventManager, GameLevel level);

    void startNextLevel(GameEventManager eventManager, GameLevel currentLevel);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    // Playing level

    void onEatPellet(GameEventManager eventManager, GameLevel level, Vector2i tile);

    void onEatEnergizer(GameEventManager eventManager, GameLevel level, Vector2i tile);

    void onEatBonus(GameEventManager eventManager, GameLevel level, Bonus bonus);

    void onEatGhost(GameEventManager eventManager, GameLevel level, Ghost eatenGhost);

    void evaluateCollisions(HuntingStepResult huntingStepResult, GameEventManager eventManager, GameLevel level);

    void activateNextBonus(GameEventManager eventManager, GameLevel level);

    void startPacPowerMode(GameEventManager eventManager, GameLevel level, Pac pac);

    void updatePacPowerMode(GameEventManager eventManager, GameLevel level, Pac pac);

    void onLevelCompleted(GameLevel level);

    // Scoring

    void scorePoints(GameEventManager eventManager, GameModel model, int points, int levelNumber);

    void updateHighScore(GameEventManager eventManager, GameModel model);
}
