/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.Ghost;

public class ArcadeCommon_ActorSpeedControl implements ActorSpeedControl {

    /** Base speed is 75px per second (=1.25 px/tick). */
    public static final float BASE_SPEED = 1.25f;

    @Override
    public float pacNormalSpeed(GameContext gameContext, GameLevel level) {
        byte pct = level.data().pacSpeedPercentage();
        return pct > 0 ? pct * 0.01f * BASE_SPEED : BASE_SPEED;
    }

    @Override
    public float pacPowerSpeed(GameContext gameContext, GameLevel level) {
        byte pct = level.data().pacSpeedPoweredPercentage();
        return pct > 0 ? pct * 0.01f * BASE_SPEED : pacNormalSpeed(gameContext, level);
    }

    @Override
    public float ghostAttackSpeed(GameContext gameContext, GameLevel level, Ghost ghost) {
        if (level.isTunnel(ghost.tile())) {
            return ghostTunnelSpeed(gameContext, level, ghost);
        }
        var arcadeGame = (ArcadeCommon_GameModel) gameContext.theGame();
        if (arcadeGame.cruiseElroy() == 1) {
            return level.data().elroy1SpeedPercentage() * 0.01f * BASE_SPEED;
        }
        if (arcadeGame.cruiseElroy() == 2) {
            return level.data().elroy2SpeedPercentage() * 0.01f * BASE_SPEED;
        }
        return level.data().ghostSpeedPercentage() * 0.01f * BASE_SPEED;
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
        float pct = level.data().ghostSpeedFrightenedPercentage();
        return pct > 0 ? pct * 0.01f * BASE_SPEED : BASE_SPEED;
    }

    @Override
    public float ghostTunnelSpeed(GameContext gameContext, GameLevel level, Ghost ghost) {
        return level.data().ghostSpeedTunnelPercentage() * 0.01f * BASE_SPEED;
    }
}
