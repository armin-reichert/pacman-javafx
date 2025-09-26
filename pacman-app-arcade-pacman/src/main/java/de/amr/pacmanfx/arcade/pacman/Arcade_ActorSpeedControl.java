/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.Ghost;

public class Arcade_ActorSpeedControl implements ActorSpeedControl {

    /** Base speed is 75 px per second (=1.25 px/tick). */
    public static final float BASE_SPEED = 1.25f;
    public static final float BASE_SPEED_1_PERCENT = 0.0125f;

    @Override
    public float pacNormalSpeed(GameLevel level) {
        byte percentage = level.data().pacSpeedPercentage();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float pacPowerSpeed(GameLevel level) {
        byte percentage = level.data().pacSpeedPoweredPercentage();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : pacNormalSpeed(level);
    }

    @Override
    public float ghostAttackSpeed(GameLevel level, Ghost ghost) {
        if (level.isTunnel(ghost.tile())) {
            return ghostTunnelSpeed(level, ghost);
        }
        var arcadeGame = (Arcade_GameModel) level.game();
        if (arcadeGame.cruiseElroy() == 1) {
            return level.data().elroy1SpeedPercentage() * BASE_SPEED_1_PERCENT;
        }
        if (arcadeGame.cruiseElroy() == 2) {
            return level.data().elroy2SpeedPercentage() * BASE_SPEED_1_PERCENT;
        }
        return level.data().ghostSpeedPercentage() * BASE_SPEED_1_PERCENT;
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
        float percentage = level.data().ghostSpeedFrightenedPercentage();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float ghostTunnelSpeed(GameLevel level, Ghost ghost) {
        return level.data().ghostSpeedTunnelPercentage() * BASE_SPEED_1_PERCENT;
    }
}
