/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.systems.common.RandomWorldMovementSystem;
import de.amr.pacmanfx.core.model.systems.ghost.*;
import de.amr.pacmanfx.core.model.systems.pac.PacDigestionSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;

public final class GameSystems {

    public final SpriteAnimSystem spriteAnim = new SpriteAnimSystem();

    public final MovementSystem motor =  new MovementSystem();
    public final WorldMovementSystem navigator = new WorldMovementSystem();
    public final RandomWorldMovementSystem roamingNavigator = new RandomWorldMovementSystem();

    public final PacPowerSystem pacPower = new PacPowerSystem();
    public final PacDigestionSystem pacDigestion = new PacDigestionSystem();

    public final GhostStateSystem ghostState = new GhostStateSystem();
    public final GhostHouseAccessSystem ghostHouseAccess = new GhostHouseAccessSystem();

    public final GhostHuntingStrategy orangeGhostPokeyHuntingStrategy = new PokeyHuntingStrategy();
    public final GhostHuntingStrategy cyanGhostBashfulHuntingStrategy = new BashfulHuntingStrategy();
    public final GhostHuntingStrategy redGhostShadowHuntingStrategy = new ShadowHuntingStrategy();
    public final GhostHuntingStrategy pinkGhostSpeedyHuntingStrategy = new SpeedyHuntingStrategy();

    public GhostHuntingStrategy ghostHuntingStrategy(byte personality) {
        return switch (personality) {
            case GameModel.RED_GHOST_SHADOW -> redGhostShadowHuntingStrategy;
            case GameModel.PINK_GHOST_SPEEDY -> pinkGhostSpeedyHuntingStrategy;
            case GameModel.CYAN_GHOST_BASHFUL -> cyanGhostBashfulHuntingStrategy;
            case GameModel.ORANGE_GHOST_POKEY -> orangeGhostPokeyHuntingStrategy;
            default -> throw new IllegalArgumentException("Unknown personality: " + personality);
        };
    }
}
