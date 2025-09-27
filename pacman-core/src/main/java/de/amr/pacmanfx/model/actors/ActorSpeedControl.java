/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.model.GameLevel;

public interface ActorSpeedControl {
    float ghostAttackSpeed(GameLevel level, Ghost ghost);
    float ghostFrightenedSpeed(GameLevel level, Ghost ghost);
    float ghostSpeedInsideHouse(GameLevel level, Ghost ghost);
    float ghostSpeedReturningToHouse(GameLevel level, Ghost ghost);
    float ghostTunnelSpeed(GameLevel level, Ghost ghost);
    float pacNormalSpeed(GameLevel level);
    float pacPowerSpeed(GameLevel level);
}
