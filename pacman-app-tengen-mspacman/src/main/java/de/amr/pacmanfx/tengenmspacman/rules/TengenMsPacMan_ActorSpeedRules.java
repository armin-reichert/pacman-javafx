/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rules;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.Validations.inClosedRange;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_ActorSpeedRules implements ActorSpeedRules {

    private Difficulty difficulty;

    public TengenMsPacMan_ActorSpeedRules() {
        difficulty = Difficulty.NORMAL;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = requireNonNull(difficulty);
    }

    @Override
    public float bonusSpeed(GameContext game, GameLevel level) {
        //TODO clarify exact speed
        return 0.5f * pacSpeed(game, level);
    }

    @Override
    public float pacSpeed(GameContext game, GameLevel level) {
        if (level == null) {
            return 0;
        }
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game.variant().gamePlay();
        final GameSession session = game.session();
        final BoosterMode boosterMode = gamePlay.boosterMode(session);

        float speed = pacBaseSpeedInLevel(level.number());
        speed += pacDifficultySpeedDelta(difficulty);
        if (boosterMode == BoosterMode.BOOSTER_ALWAYS_ON
            || boosterMode == BoosterMode.ACTIVATE_WITH_A_OR_B && gamePlay.isBoosterOn(session)) {
            speed += pacBoosterSpeedDelta();
        }
        return speed;
    }

    @Override
    public float pacSpeedWhenHasPower(GameContext game, GameLevel level) {
        //TODO correct?
        return level.entities().pac() != null ? 1.1f * pacSpeed(game, level) : 0;
    }

    @Override
    public float ghostSpeed(GameContext game, Ghost ghost) {
        final GameLevel level = game.session().level();
        final House house = level.entities().house();
        final int levelNumber = level.number();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final Vector2i ghostTile = ghost.pos().tile();
        final GhostState state = ghost.state().enumValue();
        final boolean insideHouse = house.isVisitedBy(ghost);
        final boolean tunnelSlowdown = terrain.isTunnel(ghostTile);
        return switch (state) {
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
        float speed = ghostBaseSpeedInLevel(levelNumber);
        speed += ghostDifficultySpeedDelta(difficulty);
        speed += ghostSpeedDelta(ghost.personality());
        float foodDelta = ghostSpeedIncreaseByFoodRemaining(level, difficulty);
        if (foodDelta > 0) {
            speed += foodDelta;
            Logger.debug("Ghost speed increased by {} units to {0.00} px/tick for {}", foodDelta, speed, ghost.name());
        }
        return speed;
    }

    @Override
    public float ghostSpeedFrightened(GameLevel level) {
        final int levelNumber = level.number();
        float speed = ghostBaseSpeedInLevel(levelNumber);
        speed += ghostDifficultySpeedDelta(difficulty);
        return 0.5f * speed; //TODO check with @RussianMan or disassembly
    }

    @Override
    public float ghostSpeedTunnel(int levelNumber) {
        float speed = ghostBaseSpeedInLevel(levelNumber);
        speed += ghostDifficultySpeedDelta(difficulty);
        return 0.4f * speed; //TODO check with @RussianMan or disassembly
    }

    // private methods

    private static float speedUnitsToPixels(float units) {
        return units / 32f;
    }

    private static float pacBoosterSpeedDelta() {
        return 0.5f;
    }

    private static float pacDifficultySpeedDelta(Difficulty difficulty) {
        return speedUnitsToPixels(switch (difficulty) {
            case EASY -> -4;
            case NORMAL -> 0;
            case HARD -> 12;
            case CRAZY -> 24;
        });
    }

    private static float ghostDifficultySpeedDelta(Difficulty difficulty) {
        return speedUnitsToPixels(switch (difficulty) {
            case EASY -> -8;
            case NORMAL -> 0;
            case HARD -> 16;
            case CRAZY -> 32;
        });
    }

    private static float ghostSpeedDelta(GhostPersonality personality) {
        return speedUnitsToPixels(switch (personality) {
            case RED_GHOST_SHADOW -> 3;
            case ORANGE_GHOST_POKEY -> 2;
            case CYAN_GHOST_BASHFUL -> 1;
            case PINK_GHOST_SPEEDY -> 0;
        });
    }

    /**
     * Fellow friend @RussianManSMWC told me on Discord:
     * <p>
     * By the way, there's an additional quirk regarding ghosts' speed.
     * On normal difficulty ONLY and in levels 5 and above, the ghosts become slightly faster if there are few dots remain.
     * if there are 31 or fewer dots, the speed is increased. the base increase value is 2, which is further increased
     * by 1 for every 8 dots eaten. (I should note it is in subunits. If it was times 2, that would've been crazy).
     * </p>
     */
    private static float ghostSpeedIncreaseByFoodRemaining(GameLevel level, Difficulty difficulty) {
        byte units = 0;
        if (difficulty == Difficulty.NORMAL && level.number() >= 5) {
            int dotsLeft = level.food().remainingFoodCount();
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

    private static float pacBaseSpeedInLevel(int levelNumber) {
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
    private static float ghostBaseSpeedInLevel(int levelNumber) {
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
}
