/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.systems;

import de.amr.pacmanfx.core.SpriteAnimController;
import de.amr.pacmanfx.core.entities.bonus.system.BonusMoveAndJumpSystem;
import de.amr.pacmanfx.core.entities.bonus.system.BonusStateSystem;
import de.amr.pacmanfx.core.entities.bonus.system.BonusWorldMovementPolicy;
import de.amr.pacmanfx.core.entities.ghost.system.GhostHouseAccessSystem;
import de.amr.pacmanfx.core.entities.ghost.system.GhostAnimationSelectionSystem;
import de.amr.pacmanfx.core.entities.ghost.system.GhostStateSystem;
import de.amr.pacmanfx.core.entities.ghost.system.GhostWorldMovementPolicy;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.pac.system.*;
import de.amr.pacmanfx.core.gameplay.hunt.*;
import de.amr.pacmanfx.core.gamestate.EntityUpdater;
import de.amr.pacmanfx.core.model.GhostPersonality;

public class DefaultGameSystems implements GameSystems {

    private final EntityUpdater entityUpdater = new EntityUpdater();
    private final SpriteAnimController spriteAnimController = new SpriteAnimController();

    protected MovementSystem motor =  new MovementSystem();
    protected WorldNavigationSystem navigator = new WorldNavigationSystem();
    protected RoamingSystem roamingNavigator = new RoamingSystem(navigator);


    protected WorldMovementPolicy pacWorldMovementPolicy;
    protected PacAutoSteeringSystem pacAutoSteering;
    protected PacStateSystem pacState;
    protected PacPowerSystem pacPower;
    protected PacDigestionSystem pacDigestion;
    protected PacAnimationSystem pacAnimation;

    protected GhostStateSystem ghostState;
    protected GhostHouseAccessSystem ghostHouseAccess;
    protected GhostWorldMovementPolicy ghostWorldMovementPolicy;
    protected GhostAnimationSelectionSystem ghostSpriteAnimation;

    protected GhostHuntingStrategy orangeGhostPokeyHuntingStrategy;
    protected GhostHuntingStrategy cyanGhostBashfulHuntingStrategy;
    protected GhostHuntingStrategy redGhostShadowHuntingStrategy;
    protected GhostHuntingStrategy pinkGhostSpeedyHuntingStrategy;

    protected BonusStateSystem bonusStateSystem;
    protected WorldMovementPolicy bonusWorldMovementPolicy;
    protected BonusMoveAndJumpSystem bonusMoveAndJumpSystem;

    protected LevelCounterSystem levelCounterSystem;

    public DefaultGameSystems() {
        createPacSystems();
        createGhostSystems();
        createBonusSystems();
        createLevelCounterSystem();
    }

    protected void createPacSystems() {
        pacPower = new PacPowerSystem();
        pacDigestion = new PacDigestionSystem();
        pacWorldMovementPolicy = new PacWorldMovementPolicy();
        pacAutoSteering = new PacAutoSteeringSystem();
        pacState = new PacStateSystem();
        pacAnimation = new PacAnimationSystem(spriteAnimController);
    }

    protected void createGhostSystems() {
        ghostHouseAccess = new GhostHouseAccessSystem();
        ghostWorldMovementPolicy = new GhostWorldMovementPolicy();
        ghostState = new GhostStateSystem(ghostHouseAccess);

        redGhostShadowHuntingStrategy = createShadowHuntingStrategy();
        pinkGhostSpeedyHuntingStrategy = createSpeedyHuntingStrategy();
        cyanGhostBashfulHuntingStrategy = createBashfulHuntingStrategy();
        orangeGhostPokeyHuntingStrategy = createPokeyHuntingStrategy();

        ghostSpriteAnimation = new GhostAnimationSelectionSystem();
    }

    protected void createBonusSystems() {
        bonusWorldMovementPolicy = new BonusWorldMovementPolicy();
        bonusMoveAndJumpSystem = new BonusMoveAndJumpSystem(navigator, bonusWorldMovementPolicy);
        bonusStateSystem = new BonusStateSystem();
    }

    protected void createLevelCounterSystem() {
        levelCounterSystem = new LevelCounterSystem();
    }

    @Override
    public EntityUpdater entityUpdater() {
        return entityUpdater;
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
    public SpriteAnimController spriteAnimController() {
        return spriteAnimController;
    }

    @Override
    public MovementSystem motor() {
        return motor;
    }

    @Override
    public WorldNavigationSystem worldNavigator() {
        return navigator;
    }

    @Override
    public RoamingSystem roaming() {
        return roamingNavigator;
    }

    @Override
    public PacAutoSteeringSystem pacAutoSteering() {
        return pacAutoSteering;
    }

    @Override
    public PacStateSystem pacState() {
        return pacState;
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
    public PacAnimationSystem pacAnimation() {
        return pacAnimation;
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
    public GhostAnimationSelectionSystem ghostSpriteAnimation() {
        return ghostSpriteAnimation;
    }

    @Override
    public BonusStateSystem bonusState() {
        return bonusStateSystem;
    }

    @Override
    public WorldMovementPolicy bonusWorldMovementPolicy() {
        return bonusWorldMovementPolicy;
    }

    @Override
    public BonusMoveAndJumpSystem bonusMoveAndJump() {
        return bonusMoveAndJumpSystem;
    }

    @Override
    public LevelCounterSystem levelCounterSystem() {
        return levelCounterSystem;
    }
}
