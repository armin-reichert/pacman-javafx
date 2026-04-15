/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.RedGhostShadow;
import de.amr.pacmanfx.model.world.TerrainLayer;

public class Arcade_ActorSpeedControl implements ActorSpeedControl {
    /**
     * Base speed is 75 px per second (=1.25 px/tick).
     */
    public static final float BASE_SPEED = 1.25f;
    public static final float BASE_SPEED_1_PERCENT = 0.01f * BASE_SPEED;

    @Override
    public float bonusSpeed(GameLevel level) {
        //TODO clarify exact speed
        return 0.5f * pacSpeed(level);
    }

    @Override
    public float pacSpeed(GameLevel level) {
        final LevelData data = Arcade_GameModel.levelData(level.number());
        byte percentage = data.pctPacSpeed();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float pacSpeedWhenHasPower(GameLevel level) {
        final LevelData data = Arcade_GameModel.levelData(level.number());
        byte percentage = data.pctPacSpeedPowered();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : pacSpeed(level);
    }

    @Override
    public float ghostSpeed(GameLevel level, Ghost ghost) {
        final int levelNumber = level.number();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final boolean insideHouse = terrain.house().isVisitedBy(ghost);
        final boolean insideTunnel = terrain.isTunnel(ghost.tile());
        return switch (ghost.state()) {
            case LOCKED -> insideHouse ? 0.5f : 0;
            case LEAVING_HOUSE -> 0.5f;
            case HUNTING_PAC -> insideTunnel ? ghostSpeedTunnel(levelNumber) : ghostSpeedAttacking(level, ghost);
            case FRIGHTENED -> insideTunnel ? ghostSpeedTunnel(levelNumber) : ghostSpeedFrightened(level);
            case EATEN -> 0;
            case RETURNING_HOME, ENTERING_HOUSE -> 2;
        };
    }

    @Override
    public float ghostSpeedAttacking(GameLevel level, Ghost ghost) {
        final int levelNumber = level.number();
        final LevelData data = Arcade_GameModel.levelData(levelNumber);
        if (ghost instanceof RedGhostShadow redGhostShadow) {
            return switch (redGhostShadow.elroyState().mode()) {
                case ZERO -> data.pctGhostSpeed() * BASE_SPEED_1_PERCENT;
                case ONE -> data.pctElroy1Speed() * BASE_SPEED_1_PERCENT;
                case TWO -> data.pctElroy2Speed() * BASE_SPEED_1_PERCENT;
            };
        } else {
            return data.pctGhostSpeed() * BASE_SPEED_1_PERCENT;
        }
    }

    @Override
    public float ghostSpeedFrightened(GameLevel level) {
        final int levelNumber = level.number();
        final LevelData data = Arcade_GameModel.levelData(levelNumber);
        float percentage = data.pctGhostSpeedFrightened();
        return percentage > 0 ? percentage * BASE_SPEED_1_PERCENT : BASE_SPEED;
    }

    @Override
    public float ghostSpeedTunnel(int levelNumber) {
        final LevelData data = Arcade_GameModel.levelData(levelNumber);
        return data.pctGhostSpeedTunnel() * BASE_SPEED_1_PERCENT;
    }
}
