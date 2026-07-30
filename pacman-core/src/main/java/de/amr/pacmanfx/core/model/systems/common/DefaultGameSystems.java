/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.common;

import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.systems.bonus.BonusJumpAnimationSystem;
import de.amr.pacmanfx.core.model.systems.bonus.BonusWorldMovementPolicy;
import de.amr.pacmanfx.core.model.systems.ghost.*;
import de.amr.pacmanfx.core.model.systems.pac.PacDigestionSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacWorldMovementPolicy;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;

public class DefaultGameSystems implements GameSystems {

    protected MovementSystem motor =  new MovementSystem();
    protected WorldNavigationSystem navigator = new WorldNavigationSystem(motor);
    protected RandomWorldMovementSystem roamingNavigator = new RandomWorldMovementSystem(navigator);

    protected SpriteAnimSystem spriteAnim = new SpriteAnimSystem();

    protected WorldMovementPolicy pacWorldMovementPolicy;
    protected PacPowerSystem pacPower;
    protected PacDigestionSystem pacDigestion;

    protected GhostStateSystem ghostState;
    protected GhostHouseAccessSystem ghostHouseAccess;
    protected GhostWorldMovementPolicy ghostWorldMovementPolicy;

    protected GhostHuntingStrategy orangeGhostPokeyHuntingStrategy;
    protected GhostHuntingStrategy cyanGhostBashfulHuntingStrategy;
    protected GhostHuntingStrategy redGhostShadowHuntingStrategy;
    protected GhostHuntingStrategy pinkGhostSpeedyHuntingStrategy;

    protected WorldMovementPolicy bonusWorldMovementPolicy;
    protected BonusJumpAnimationSystem bonusJumpAnimationSystem;

    public DefaultGameSystems() {
        createPacSystems();
        createGhostSystems();
        createBonusSystems(navigator);
    }

    protected void createPacSystems() {
        pacPower = new PacPowerSystem();
        pacDigestion = new PacDigestionSystem();
        pacWorldMovementPolicy = new PacWorldMovementPolicy();
    }

    protected void createGhostSystems() {
        ghostState = new GhostStateSystem();
        ghostHouseAccess = new GhostHouseAccessSystem();
        ghostWorldMovementPolicy = new GhostWorldMovementPolicy();

        redGhostShadowHuntingStrategy = createShadowHuntingStrategy();
        pinkGhostSpeedyHuntingStrategy = createSpeedyHuntingStrategy();
        cyanGhostBashfulHuntingStrategy = createBashfulHuntingStrategy();
        orangeGhostPokeyHuntingStrategy = createPokeyHuntingStrategy();
    }

    protected void createBonusSystems(WorldNavigationSystem navigator) {
        bonusWorldMovementPolicy = new BonusWorldMovementPolicy();
        bonusJumpAnimationSystem = new BonusJumpAnimationSystem(navigator, bonusWorldMovementPolicy);
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

    public WorldMovementPolicy pacWorldMovementPolicy() {
        return pacWorldMovementPolicy;
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
    public GhostWorldMovementPolicy ghostWorldMovementPolicy() {
        return ghostWorldMovementPolicy;
    }

    @Override
    public GhostHuntingStrategy ghostHuntingStrategy(GhostPersonality personality) {
        return switch (personality) {
            case RED_GHOST_SHADOW -> redGhostShadowHuntingStrategy;
            case PINK_GHOST_SPEEDY -> pinkGhostSpeedyHuntingStrategy;
            case CYAN_GHOST_BASHFUL -> cyanGhostBashfulHuntingStrategy;
            case ORANGE_GHOST_POKEY -> orangeGhostPokeyHuntingStrategy;
        };
    }

    @Override
    public WorldMovementPolicy bonusWorldMovementPolicy() {
        return bonusWorldMovementPolicy;
    }

    public BonusJumpAnimationSystem bonusJumpAnimation() {
        return bonusJumpAnimationSystem;
    }
}
