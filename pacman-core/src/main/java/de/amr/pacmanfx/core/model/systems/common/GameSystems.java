/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.common;

import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.systems.bonus.BonusJumpAnimationSystem;
import de.amr.pacmanfx.core.model.systems.ghost.GhostHouseAccessSystem;
import de.amr.pacmanfx.core.model.systems.ghost.GhostHuntingStrategy;
import de.amr.pacmanfx.core.model.systems.ghost.GhostStateSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacDigestionSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;

public interface GameSystems {

    SpriteAnimSystem spriteAnim();

    MovementSystem motor();

    WorldNavigationSystem navigator();

    RandomWorldMovementSystem roamingNavigator();

    WorldMovementPolicy pacWorldMovementPolicy();

    PacPowerSystem pacPower();

    PacDigestionSystem pacDigestion();

    GhostStateSystem ghostState();

    GhostHouseAccessSystem ghostHouseAccess();

    WorldMovementPolicy ghostWorldMovementPolicy();

    GhostHuntingStrategy ghostHuntingStrategy(GhostPersonality personality);

    WorldMovementPolicy bonusWorldMovementPolicy();

    BonusJumpAnimationSystem bonusJumpAnimation();
}