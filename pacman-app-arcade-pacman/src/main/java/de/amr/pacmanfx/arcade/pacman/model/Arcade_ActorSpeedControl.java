/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.core.Globals_Core;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.TerrainLayer;

public class Arcade_ActorSpeedControl implements ActorSpeedControl {

    /** Base speed is 75 px per second (=1.25 px/tick). */
    public static final float BASE_SPEED = 1.25f;

    /** 1 percent of base speed. */
    public static final float BASE_SPEED_ONE_PERCENT = 0.01f * BASE_SPEED;

    @Override
    public float bonusSpeed(GameLevel level) {
        //TODO clarify exact speed in emulator
        return 0.5f * pacSpeed(level);
    }

    @Override
    public float pacSpeed(GameLevel level) {
        final byte pct = ArcadePacMan_GameRules.levelData(level.number()).pctPacSpeed();
        return pct > 0 ? pct * BASE_SPEED_ONE_PERCENT : BASE_SPEED;
    }

    @Override
    public float pacSpeedWhenHasPower(GameLevel level) {
        final byte pct = ArcadePacMan_GameRules.levelData(level.number()).pctPacSpeedPowered();
        return pct > 0 ? pct * BASE_SPEED_ONE_PERCENT : pacSpeed(level);
    }

    @Override
    public float ghostSpeed(GameLevel level, Ghost ghost) {
        final int levelNumber = level.number();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final boolean insideHouse = terrain.house().isVisitedBy(ghost);
        final boolean tunnelSlowdown = terrain.isTunnel(ghost.computeTile());
        return switch (ghost.state()) {
            case LOCKED -> insideHouse ? 0.5f : 0;
            case LEAVING_HOUSE -> 0.5f;
            case HUNTING_PAC -> tunnelSlowdown ? ghostSpeedTunnel(levelNumber) : ghostSpeedAttacking(level, ghost);
            case FRIGHTENED -> tunnelSlowdown ? ghostSpeedTunnel(levelNumber) : ghostSpeedFrightened(level);
            case EATEN -> 0;
            case RETURNING_HOME, ENTERING_HOUSE -> 2;
        };
    }

    @Override
    public float ghostSpeedAttacking(GameLevel level, Ghost ghost) {
        final int levelNumber = level.number();
        final LevelData data = ArcadePacMan_GameRules.levelData(levelNumber);
        if (ghost.personality() == Globals_Core.RED_GHOST_SHADOW) {
            return switch (ghost.elroy().boost()) {
                case NONE -> data.pctGhostSpeed()  * BASE_SPEED_ONE_PERCENT;
                case MEDIUM -> data.pctElroy1Speed() * BASE_SPEED_ONE_PERCENT;
                case LARGE -> data.pctElroy2Speed() * BASE_SPEED_ONE_PERCENT;
            };
        } else {
            return data.pctGhostSpeed() * BASE_SPEED_ONE_PERCENT;
        }
    }

    @Override
    public float ghostSpeedFrightened(GameLevel level) {
        final float pct = ArcadePacMan_GameRules.levelData(level.number()).pctGhostSpeedFrightened();
        return pct > 0 ? pct * BASE_SPEED_ONE_PERCENT : BASE_SPEED;
    }

    @Override
    public float ghostSpeedTunnel(int levelNumber) {
        return ArcadePacMan_GameRules.levelData(levelNumber).pctGhostSpeedTunnel() * BASE_SPEED_ONE_PERCENT;
    }
}
