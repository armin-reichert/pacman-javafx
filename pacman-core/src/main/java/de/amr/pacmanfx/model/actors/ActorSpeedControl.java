/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.model.GameLevel;

public interface ActorSpeedControl {
    float ghostAttackSpeed(GameContext gameContext, GameLevel level, Ghost ghost);
    float ghostFrightenedSpeed(GameContext gameContext, GameLevel level, Ghost ghost);
    float ghostSpeedInsideHouse(GameContext gameContext, GameLevel level, Ghost ghost);
    float ghostSpeedReturningToHouse(GameContext gameContext, GameLevel level, Ghost ghost);
    float ghostTunnelSpeed(GameContext gameContext, GameLevel level, Ghost ghost);
    float pacNormalSpeed(GameContext gameContext, GameLevel level);
    float pacPowerSpeed(GameContext gameContext, GameLevel level);
}
