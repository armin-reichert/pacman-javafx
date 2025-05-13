/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.model.GameException;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.Ghost;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.*;

public class TengenActorSpeedControl implements ActorSpeedControl {

    private final TengenMsPacMan_GameModel game;

    public TengenActorSpeedControl(TengenMsPacMan_GameModel game) {
        this.game = game;
    }

    public float speedUnitsToPixels(float units) {
        return units / 32f;
    }

    public float pacBoosterSpeedDelta() {
        return 0.5f;
    }

    public float pacDifficultySpeedDelta(Difficulty difficulty) {
        return speedUnitsToPixels(switch (difficulty) {
            case EASY -> -4;
            case NORMAL -> 0;
            case HARD -> 12;
            case CRAZY -> 24;
        });
    }

    public float ghostDifficultySpeedDelta(Difficulty difficulty) {
        return speedUnitsToPixels(switch (difficulty) {
            case EASY -> -8;
            case NORMAL -> 0;
            case HARD -> 16;
            case CRAZY -> 32;
        });
    }

    public float ghostIDSpeedDelta(byte ghostID) {
        return speedUnitsToPixels(switch (ghostID) {
            case RED_GHOST_SHADOW -> 3;
            case ORANGE_GHOST_POKEY -> 2;
            case CYAN_GHOST_BASHFUL -> 1;
            case PINK_GHOST_SPEEDY -> 0;
            default -> throw GameException.invalidGhostID(ghostID);
        });
    }

    /**
     * Fellow friend @RussianManSMWC told me on Discord:
     * <p>
     * By the way, there's an additional quirk regarding ghosts' speed.
     * On normal difficulty ONLY and in levels 5 and above, the ghosts become slightly faster if there are few dots remain.
     * if there are 31 or less dots, the speed is increased. the base increase value is 2, which is further increased by 1 for every 8 dots eaten.
     * (I should note it's in subunits. it if was times 2, that would've been crazy)
     * </p>
     */
    public float ghostSpeedIncreaseByFoodRemaining(GameLevel level) {
        byte units = 0;
        if (game.difficulty() == Difficulty.NORMAL && level.number() >= 5) {
            int dotsLeft = level.uneatenFoodCount();
            if (dotsLeft <= 7) {
                units = 5;
            } else if (dotsLeft <= 15) {
                units = 4;
            } else if (dotsLeft <= 23) {
                units = 3;
            } else if (dotsLeft <= 31) {
                units = 2;
            }
        }
        return speedUnitsToPixels(units);
    }

    public float pacBaseSpeedInLevel(int levelNumber) {
        int units = 0;
        if (Validations.inClosedRange(levelNumber, 1, 4)) {
            units = 0x20;
        } else if (Validations.inClosedRange(levelNumber, 5, 12)) {
            units = 0x24;
        } else if (Validations.inClosedRange(levelNumber, 13, 16)) {
            units = 0x28;
        } else if (Validations.inClosedRange(levelNumber, 17, 20)) {
            units = 0x27;
        } else if (Validations.inClosedRange(levelNumber, 21, 24)) {
            units = 0x26;
        } else if (Validations.inClosedRange(levelNumber, 25, 28)) {
            units = 0x25;
        } else if (levelNumber >= 29) {
            units = 0x24;
        }

        return speedUnitsToPixels(units);
    }

    // TODO: do they all have the same base speed? Unclear from disassembly data.
    public float ghostBaseSpeedInLevel(int levelNumber) {
        int units = 0x20; // default: 32
        if (Validations.inClosedRange(levelNumber, 1, 4)) {
            units = 0x18;
        } else if (Validations.inClosedRange(levelNumber, 5, 12)) {
            units = 0x20 + (levelNumber - 5);
        } // 0x20-0x27
        else if (levelNumber >= 13) {
            units = 0x28;
        }
        return speedUnitsToPixels(units);
    }

    @Override
    public float pacNormalSpeed(GameLevel level) {
        return level.pac() != null ? level.pac().baseSpeed() : 0;
    }

    @Override
    public float pacPowerSpeed(GameLevel level) {
        //TODO is this correct?
        return 1.1f * level.pac().baseSpeed();
    }

    @Override
    public float ghostAttackSpeed(GameLevel level, Ghost ghost) {
        if (level.isTunnel(ghost.tile())) {
            return ghostTunnelSpeed(level, ghost);
        }
        float speed = ghost.baseSpeed();
        float increase = ghostSpeedIncreaseByFoodRemaining(level);
        if (increase > 0) {
            speed += increase;
            Logger.debug("Ghost speed increased by {} units to {0.00} px/tick for {}", increase, speed, ghost.name());
        }
        return speed;
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
        //TODO is this correct?
        return 0.5f * ghost.baseSpeed();
    }

    @Override
    public float ghostTunnelSpeed(GameLevel level, Ghost ghost) {
        //TODO is this correct?
        return 0.4f * ghost.baseSpeed();
    }
}
