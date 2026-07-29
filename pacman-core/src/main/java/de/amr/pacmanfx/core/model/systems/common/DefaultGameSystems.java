/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.common;

import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.systems.ghost.*;
import de.amr.pacmanfx.core.model.systems.pac.PacDigestionSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;

public final class DefaultGameSystems implements GameSystems {

    private final SpriteAnimSystem spriteAnim = new SpriteAnimSystem();

    private final MovementSystem motor =  new MovementSystem();
    private final WorldNavigationSystem navigator = new WorldNavigationSystem(motor);
    private final RandomWorldMovementSystem roamingNavigator = new RandomWorldMovementSystem(navigator);

    private final PacPowerSystem pacPower = new PacPowerSystem();
    private final PacDigestionSystem pacDigestion = new PacDigestionSystem();

    private final GhostStateSystem ghostState = new GhostStateSystem();
    private final GhostHouseAccessSystem ghostHouseAccess = new GhostHouseAccessSystem();

    private final GhostHuntingStrategy orangeGhostPokeyHuntingStrategy = new PokeyHuntingStrategy(navigator);
    private final GhostHuntingStrategy cyanGhostBashfulHuntingStrategy = new BashfulHuntingStrategy(navigator);
    private final GhostHuntingStrategy redGhostShadowHuntingStrategy = new ShadowHuntingStrategy(navigator);
    private final GhostHuntingStrategy pinkGhostSpeedyHuntingStrategy = new SpeedyHuntingStrategy(navigator);

    @Override
    public SpriteAnimSystem spriteAnim() {
        return spriteAnim;
    }

    @Override
    public MovementSystem motor() {
        return motor;
    }

    @Override
    public WorldNavigationSystem navigator() {
        return navigator;
    }

    @Override
    public RandomWorldMovementSystem roamingNavigator() {
        return roamingNavigator;
    }

    @Override
    public PacPowerSystem pacPower() {
        return pacPower;
    }

    @Override
    public PacDigestionSystem pacDigestion() {
        return pacDigestion;
    }

    @Override
    public GhostStateSystem ghostState() {
        return ghostState;
    }

    @Override
    public GhostHouseAccessSystem ghostHouseAccess() {
        return ghostHouseAccess;
    }

    @Override
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
