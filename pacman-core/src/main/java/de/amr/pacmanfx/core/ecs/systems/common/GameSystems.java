/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.systems.common;

import de.amr.pacmanfx.core.ecs.systems.pac.*;
import de.amr.pacmanfx.core.ecs.systems.world.RandomWorldMovementSystem;
import de.amr.pacmanfx.core.ecs.systems.world.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.ecs.systems.bonus.BonusMoveAndJumpSystem;
import de.amr.pacmanfx.core.ecs.systems.bonus.BonusStateSystem;
import de.amr.pacmanfx.core.ecs.systems.ghost.GhostHouseAccessSystem;
import de.amr.pacmanfx.core.ecs.systems.ghost.GhostHuntingStrategy;
import de.amr.pacmanfx.core.ecs.systems.ghost.GhostStateSystem;
import de.amr.pacmanfx.core.ecs.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.core.ecs.systems.world.WorldMovementPolicy;

public interface GameSystems {

    SpriteAnimSystem spriteAnim();

    MovementSystem motor();

    WorldNavigationSystem worldNavigator();

    RandomWorldMovementSystem roamingNavigator();

    WorldMovementPolicy pacWorldMovementPolicy();

    PacAutoSteeringSystem pacAutoSteering();

    PacStateSystem pacState();

    PacPowerSystem pacPower();

    PacDigestionSystem pacDigestion();

    PacAnimationSystem pacAnimation();

    GhostStateSystem ghostState();

    GhostHouseAccessSystem ghostHouseAccess();

    WorldMovementPolicy ghostWorldMovementPolicy();

    GhostHuntingStrategy ghostHuntingStrategy(GhostPersonality personality);

    BonusStateSystem bonusState();

    WorldMovementPolicy bonusWorldMovementPolicy();

    BonusMoveAndJumpSystem bonusMoveAndJump();
}