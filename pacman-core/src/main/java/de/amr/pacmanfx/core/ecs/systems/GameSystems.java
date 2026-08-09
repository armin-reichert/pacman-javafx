/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.systems;

import de.amr.pacmanfx.core.entities.bonus.system.BonusMoveAndJumpSystem;
import de.amr.pacmanfx.core.entities.bonus.system.BonusStateSystem;
import de.amr.pacmanfx.core.entities.ghost.system.GhostHouseAccessSystem;
import de.amr.pacmanfx.core.entities.ghost.system.GhostStateSystem;
import de.amr.pacmanfx.core.entities.pac.system.*;
import de.amr.pacmanfx.core.gameplay.GhostHuntingStrategy;
import de.amr.pacmanfx.core.model.GhostPersonality;

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