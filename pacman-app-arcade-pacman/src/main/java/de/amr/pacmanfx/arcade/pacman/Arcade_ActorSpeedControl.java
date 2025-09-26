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
    public float pacNormalSpeed(GameLevel gameLevel) {
        byte percentage = gameLevel.data().pacSpeedPercentage();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float pacPowerSpeed(GameLevel gameLevel) {
        byte percentage = gameLevel.data().pacSpeedPoweredPercentage();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : pacNormalSpeed(gameLevel);
    }

    @Override
    public float ghostAttackSpeed(GameLevel gameLevel, Ghost ghost) {
        if (gameLevel.isTunnel(ghost.tile())) {
            return ghostTunnelSpeed(gameLevel, ghost);
        }
        var arcadeGame = (Arcade_GameModel) gameLevel.game();
        if (arcadeGame.cruiseElroy() == 1) {
            return gameLevel.data().elroy1SpeedPercentage() * BASE_SPEED_1_PERCENT;
        }
        if (arcadeGame.cruiseElroy() == 2) {
            return gameLevel.data().elroy2SpeedPercentage() * BASE_SPEED_1_PERCENT;
        }
        return gameLevel.data().ghostSpeedPercentage() * BASE_SPEED_1_PERCENT;
    }

    @Override
    public float ghostSpeedInsideHouse(GameLevel gameLevel, Ghost ghost) {
        return 0.5f;
    }

    @Override
    public float ghostSpeedReturningToHouse(GameLevel gameLevel, Ghost ghost) {
        return 2;
    }

    @Override
    public float ghostFrightenedSpeed(GameLevel gameLevel, Ghost ghost) {
        float percentage = gameLevel.data().ghostSpeedFrightenedPercentage();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float ghostTunnelSpeed(GameLevel gameLevel, Ghost ghost) {
        return gameLevel.data().ghostSpeedTunnelPercentage() * BASE_SPEED_1_PERCENT;
    }
}