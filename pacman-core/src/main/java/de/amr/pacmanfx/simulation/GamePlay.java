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

    boolean canStartNewGame(GameContext context);

    void resetForNewGame(GameModel model);

    void prepareLevelForPlaying(GameLevel level);

    // Level building and level start

    GameLevel buildDemoLevel(GameEventManager eventManager, GameModel model);

    boolean isDemoLevelRunning(GameContext context);

    boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    void buildNormalLevel(GameEventManager eventManager, GameModel model, int levelNumber);

    void startLevel(GameEventManager eventManager, GameModel model, GameLevel level);

    void startNextLevel(GameEventManager eventManager, GameModel model, GameLevel currentLevel);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    // Playing level

    void eatPellet(GameEventManager eventManager, GameModel model, GameLevel level, Vector2i tile);

    void eatEnergizer(GameEventManager eventManager, GameModel model, GameLevel level, Vector2i tile);

    void eatBonus(GameEventManager eventManager, GameModel model, GameLevel level, Bonus bonus);

    void eatGhost(GameEventManager eventManager, GameModel model, GameLevel level, Ghost eatenGhost);

    void activateNextBonus(GameEventManager eventManager, GameModel model, GameLevel level);

    void startPacPowerMode(GameEventManager eventManager, GameModel model, GameLevel level, Pac pac);

    void updatePacPowerMode(GameEventManager eventManager, GameModel model, GameLevel level, Pac pac);

    void onLevelCompleted(GameLevel level);

    // Scoring

    void scorePoints(GameEventManager eventManager, GameModel model, int points, int levelNumber);

    void updateHighScore(GameEventManager eventManager, GameModel model);
}
