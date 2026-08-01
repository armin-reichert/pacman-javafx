/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.common;

import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.systems.bonus.BonusMoveAndJumpSystem;
import de.amr.pacmanfx.core.model.systems.bonus.BonusStateSystem;
import de.amr.pacmanfx.core.model.systems.ghost.GhostHouseAccessSystem;
import de.amr.pacmanfx.core.model.systems.ghost.GhostHuntingStrategy;
import de.amr.pacmanfx.core.model.systems.ghost.GhostStateSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacAnimationSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacDigestionSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacStateSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;

public interface GameSystems {

    SpriteAnimSystem spriteAnim();

    MovementSystem motor();

    WorldNavigationSystem worldNavigator();

    RandomWorldMovementSystem roamingNavigator();

    WorldMovementPolicy pacWorldMovementPolicy();

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