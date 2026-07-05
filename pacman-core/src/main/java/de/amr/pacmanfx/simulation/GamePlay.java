/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.simulation;


import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;

public interface GamePlay {

    void eatPellet(GameContext context, GameLevel level, Vector2i tile);

    void eatEnergizer(GameContext context, GameLevel level, Vector2i tile);

    void eatBonus(GameContext gameContext, GameLevel level, Bonus bonus);

    void onEatGhost(GameContext gameContext, GameLevel level, Ghost eatenGhost);

    void activateNextBonus(GameContext context, GameLevel level);

    void startPacPowerMode(GameContext context, GameLevel level, Pac pac);

    void updatePacPowerMode(GameContext gameContext, GameLevel level, Pac pac);

    boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    void onLevelCompleted(GameLevel level);

    boolean isDemoLevelRunning(GameContext context);

    void prepareLevelForPlaying(GameLevel level);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    void startNextLevel(GameContext gameContext, GameLevel currentLevel);
}
