/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.model.GameException;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.Ghost;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.inClosedRange;

public class TengenActorSpeedControl implements ActorSpeedControl {

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

    public float ghostSpeedDelta(byte personality) {
        return speedUnitsToPixels(switch (personality) {
            case RED_GHOST_SHADOW -> 3;
            case ORANGE_GHOST_POKEY -> 2;
            case CYAN_GHOST_BASHFUL -> 1;
            case PINK_GHOST_SPEEDY -> 0;
            default -> throw GameException.invalidGhostPersonality(personality);
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
    public float ghostSpeedIncreaseByFoodRemaining(GameContext gameContext, GameLevel level) {
        byte units = 0;
        TengenMsPacMan_GameModel game = gameContext.game();
        if (game.difficulty() == Difficulty.NORMAL && level.number() >= 5) {
            int dotsLeft = level.foodStore().uneatenFoodCount();
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
        if (inClosedRange(levelNumber, 1, 4)) {
            units = 0x20;
        } else if (inClosedRange(levelNumber, 5, 12)) {
            units = 0x24;
        } else if (inClosedRange(levelNumber, 13, 16)) {
            units = 0x28;
        } else if (inClosedRange(levelNumber, 17, 20)) {
            units = 0x27;
        } else if (inClosedRange(levelNumber, 21, 24)) {
            units = 0x26;
        } else if (inClosedRange(levelNumber, 25, 28)) {
            units = 0x25;
        } else if (levelNumber >= 29) {
            units = 0x24;
        }

        return speedUnitsToPixels(units);
    }

    // TODO: do they all have the same base speed? Unclear from disassembly data.
    public float ghostBaseSpeedInLevel(int levelNumber) {
        int units = 0x20; // default: 32
        if (inClosedRange(levelNumber, 1, 4)) {
            units = 0x18;
        } else if (inClosedRange(levelNumber, 5, 12)) {
            units = 0x20 + (levelNumber - 5);
        } // 0x20-0x27
        else if (levelNumber >= 13) {
            units = 0x28;
        }
        return speedUnitsToPixels(units);
    }

    @Override
    public float pacNormalSpeed(GameContext gameContext, GameLevel level) {
        if (level == null) {
            return 0;
        }
        TengenMsPacMan_GameModel game = gameContext.game();
        float speed = pacBaseSpeedInLevel(level.number());
        speed += pacDifficultySpeedDelta(game.difficulty());
        if (game.pacBooster() == PacBooster.ALWAYS_ON
            || game.pacBooster() == PacBooster.USE_A_OR_B && game.isBoosterActive()) {
            speed += pacBoosterSpeedDelta();
        }
        return speed;
    }

    @Override
    public float pacPowerSpeed(GameContext gameContext, GameLevel level) {
        //TODO is this correct?
        return level.pac() != null ? 1.1f * pacNormalSpeed(gameContext, level) : 0;
    }

    @Override
    public float ghostAttackSpeed(GameContext gameContext, GameLevel level, Ghost ghost) {
        if (level.isTunnel(ghost.tile())) {
            return ghostTunnelSpeed(gameContext, level, ghost);
        }
        TengenMsPacMan_GameModel game = gameContext.game();
        float speed = ghostBaseSpeedInLevel(level.number());
        speed += ghostDifficultySpeedDelta(game.difficulty());
        speed += ghostSpeedDelta(ghost.id().personality());
        float foodDelta = ghostSpeedIncreaseByFoodRemaining(gameContext, level);
        if (foodDelta > 0) {
            speed += foodDelta;
            Logger.debug("Ghost speed increased by {} units to {0.00} px/tick for {}", foodDelta, speed, ghost.name());
        }
        return speed;
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
        //TODO is this correct?
        return 0.5f * ghostAttackSpeed(gameContext, level, ghost);
    }

    @Override
    public float ghostTunnelSpeed(GameContext gameContext, GameLevel level, Ghost ghost) {
        //TODO is this correct?
        return 0.4f;
    }
}