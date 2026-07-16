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

    void init(GameContext gameContext);

    void resetForNewGame(GameContext gameContext);

    void prepareLevelForPlaying(GameContext gameContext);

    // Level building and level start

    GameLevel createLevel(GameModel model, int levelNumber, boolean demoLevel);

    GameLevel buildDemoLevel(GameContext context);

    void buildNormalLevel(GameContext context, int levelNumber);

    boolean isDemoLevelRunning(GameModel model);

    boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    void startLevel(GameContext context);

    void startNextLevel(GameContext context);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    // Playing level

    void hunt(GameContext context);

    void onEatPellet(GameContext context, Vector2i tile);

    void onEatEnergizer(GameContext context, Vector2i tile);

    void onEatBonus(GameContext context, Bonus bonus);

    void onEatGhost(GameContext context, Ghost eatenGhost);

    void activateNextBonus(GameContext context);

    void startPacPowerMode(GameContext context, Pac pac);

    void updatePacPowerMode(GameContext context, Pac pac);

    void onLevelCompleted(GameLevel level);

    // Scoring

    void scorePoints(GameContext context, int points, int levelNumber);

    void updateHighScore(GameContext context);
}
