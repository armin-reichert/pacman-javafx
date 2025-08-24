/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.Ghost;

public class Arcade_ActorSpeedControl implements ActorSpeedControl {

    /** Base speed is 75 px per second (=1.25 px/tick). */
    public static final float BASE_SPEED = 1.25f;
    public static final float BASE_SPEED_1_PERCENT = 0.0125f;

    @Override
    public float pacNormalSpeed(GameContext gameContext, GameLevel level) {
        byte percentage = level.data().pacSpeedPercentage();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float pacPowerSpeed(GameContext gameContext, GameLevel level) {
        byte percentage = level.data().pacSpeedPoweredPercentage();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : pacNormalSpeed(gameContext, level);
    }

    @Override
    public float ghostAttackSpeed(GameContext gameContext, GameLevel level, Ghost ghost) {
        if (level.isTunnel(ghost.tile())) {
            return ghostTunnelSpeed(gameContext, level, ghost);
        }
        var arcadeGame = (Arcade_GameModel) gameContext.game();
        if (arcadeGame.cruiseElroy() == 1) {
            return level.data().elroy1SpeedPercentage() * BASE_SPEED_1_PERCENT;
        }
        if (arcadeGame.cruiseElroy() == 2) {
            return level.data().elroy2SpeedPercentage() * BASE_SPEED_1_PERCENT;
        }
        return level.data().ghostSpeedPercentage() * BASE_SPEED_1_PERCENT;
    }

    @Override
    public float ghostSpeedInsideHouse(GameContext gameContext, GameLevel level, Ghost ghost) {
        return 0.5f;
    }

    @Override
    public float ghostSpeedReturningToHouse(GameContext gameContext, GameLevel level, Ghost ghost) {
        return 2;
    }

    @Override
    public float ghostFrightenedSpeed(GameContext gameContext, GameLevel level, Ghost ghost) {
        float percentage = level.data().ghostSpeedFrightenedPercentage();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float ghostTunnelSpeed(GameContext gameContext, GameLevel level, Ghost ghost) {
        return level.data().ghostSpeedTunnelPercentage() * BASE_SPEED_1_PERCENT;
    }
}
