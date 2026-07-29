/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.common;


import de.amr.pacmanfx.core.model.systems.ghost.GhostHouseAccessSystem;
import de.amr.pacmanfx.core.model.systems.ghost.GhostHuntingStrategy;
import de.amr.pacmanfx.core.model.systems.ghost.GhostStateSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacDigestionSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;

public interface GameSystems {
    SpriteAnimSystem spriteAnim();

    MovementSystem motor();

    WorldNavigationSystem navigator();

    RandomWorldMovementSystem roamingNavigator();

    PacPowerSystem pacPower();

    PacDigestionSystem pacDigestion();

    GhostStateSystem ghostState();

    GhostHouseAccessSystem ghostHouseAccess();

    GhostHuntingStrategy ghostHuntingStrategy(byte personality);
}