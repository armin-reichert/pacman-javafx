/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.common;

import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.systems.ghost.*;
import de.amr.pacmanfx.core.model.systems.pac.PacDigestionSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;

public class DefaultGameSystems implements GameSystems {

    protected SpriteAnimSystem spriteAnim = new SpriteAnimSystem();

    protected MovementSystem motor =  new MovementSystem();
    protected WorldNavigationSystem navigator = new WorldNavigationSystem(motor);
    protected RandomWorldMovementSystem roamingNavigator = new RandomWorldMovementSystem(navigator);

    protected PacPowerSystem pacPower;
    protected PacDigestionSystem pacDigestion;

    protected GhostStateSystem ghostState;
    protected GhostHouseAccessSystem ghostHouseAccess;

    protected GhostHuntingStrategy orangeGhostPokeyHuntingStrategy;
    protected GhostHuntingStrategy cyanGhostBashfulHuntingStrategy;
    protected GhostHuntingStrategy redGhostShadowHuntingStrategy;
    protected GhostHuntingStrategy pinkGhostSpeedyHuntingStrategy;

    public DefaultGameSystems() {
        createPacSystems();
        createGhostSystems();
    }

    protected void createPacSystems() {
        pacPower = new PacPowerSystem();
        pacDigestion = new PacDigestionSystem();
    }

    protected void createGhostSystems() {
        ghostState = new GhostStateSystem();
        ghostHouseAccess = new GhostHouseAccessSystem();

        redGhostShadowHuntingStrategy = createShadowHuntingStrategy();
        pinkGhostSpeedyHuntingStrategy = createSpeedyHuntingStrategy();
        cyanGhostBashfulHuntingStrategy = createBashfulHuntingStrategy();
        orangeGhostPokeyHuntingStrategy = createPokeyHuntingStrategy();
    }

    /**
     * @return strategy used by the red ghost ("Shadow")
     */
    protected GhostHuntingStrategy createShadowHuntingStrategy() {
        return new ShadowHuntingStrategy(navigator);
    }

    /**
     * @return strategy used by the pink ghost ("Speedy")
     */
    protected GhostHuntingStrategy createSpeedyHuntingStrategy() {
        return  new SpeedyHuntingStrategy(navigator);
    }

    /**
     * @return strategy used by the cyan ghost ("Bashful")
     */
    protected GhostHuntingStrategy createBashfulHuntingStrategy() {
        return new BashfulHuntingStrategy(navigator);
    }

    /**
     * @return strategy used by the orange ghost ("Pokey")
     */
    protected GhostHuntingStrategy createPokeyHuntingStrategy() {
        return new PokeyHuntingStrategy(navigator);
    }

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
