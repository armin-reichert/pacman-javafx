/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.ecs.systems.*;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.system.BonusMoveAndJumpSystem;
import de.amr.pacmanfx.core.entities.bonus.system.BonusStateSystem;
import de.amr.pacmanfx.core.entities.ghost.system.GhostAnimationSystem;
import de.amr.pacmanfx.core.entities.ghost.system.GhostHouseAccessSystem;
import de.amr.pacmanfx.core.entities.ghost.system.GhostStateSystem;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.pac.system.*;
import de.amr.pacmanfx.core.gameplay.hunt.GhostHuntingStrategy;
import de.amr.pacmanfx.core.gamestate.EntityUpdater;
import de.amr.pacmanfx.core.model.GhostPersonality;

public interface GameSystems {

    EntityUpdater entityUpdater();

    ActorSpriteAnimController actorSpriteAnimController();

    MovementSystem motor();

    WorldNavigationSystem worldNavigator();

    RoamingSystem roaming();

    WorldMovementPolicy<Pac> pacWorldMovementPolicy();

    PacAutoSteeringSystem pacAutoSteering();

    PacStateSystem pacState();

    PacPowerSystem pacPower();

    PacDigestionSystem pacDigestion();

    PacAnimationSystem pacAnimation();

    GhostStateSystem ghostState();

    GhostAnimationSystem ghostAnimation();

    GhostHouseAccessSystem ghostHouseAccess();

    WorldMovementPolicy<Ghost> ghostWorldMovementPolicy();

    GhostHuntingStrategy ghostHuntingStrategy(GhostPersonality personality);

    BonusStateSystem bonusState();

    WorldMovementPolicy<Bonus> bonusWorldMovementPolicy();

    BonusMoveAndJumpSystem bonusMoveAndJump();

    LevelCounterSystem levelCounterSystem();
}