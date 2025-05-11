/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.Ghost;

public class ArcadeAny_ActorSpeedControl implements ActorSpeedControl {
    public static final float BASE_SPEED = 1.25f;

    @Override
    public float pacNormalSpeed(GameLevel level) {
        byte pct = level.data().pacSpeedPercentage();
        return pct > 0 ? pct * 0.01f * level.pac().baseSpeed() : level.pac().baseSpeed();
    }

    @Override
    public float pacPowerSpeed(GameLevel level) {
        byte pct = level.data().pacSpeedPoweredPercentage();
        return pct > 0 ? pct * 0.01f * level.pac().baseSpeed() : pacNormalSpeed(level);
    }

    @Override
    public float ghostAttackSpeed(GameLevel level, Ghost ghost) {
        if (level.isTunnel(ghost.tile())) {
            return level.data().ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed();
        }
        if (ghost.cruiseElroy() == 1) {
            return level.data().elroy1SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        if (ghost.cruiseElroy() == 2) {
            return level.data().elroy2SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        return level.data().ghostSpeedPercentage() * 0.01f * ghost.baseSpeed();
    }

    @Override
    public float ghostSpeedInsideHouse(GameLevel level, Ghost ghost) {
        return 0.5f;
    }

    @Override
    public float ghostSpeedReturningToHouse(GameLevel level, Ghost ghost) {
        return 2;
    }

    @Override
    public float ghostFrightenedSpeed(GameLevel level, Ghost ghost) {
        float pct = level.data().ghostSpeedFrightenedPercentage();
        return pct > 0 ? pct * 0.01f * ghost.baseSpeed() : ghost.baseSpeed();
    }

    @Override
    public float ghostTunnelSpeed(GameLevel level, Ghost ghost) {
        return level.data().ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed();
    }
}
