/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.ecs.systems.*;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.system.BonusMoveAndJumpSystem;
import de.amr.pacmanfx.core.entities.bonus.system.BonusStateSystem;
import de.amr.pacmanfx.core.entities.bonus.system.BonusWorldMovementPolicy;
import de.amr.pacmanfx.core.entities.ghost.system.*;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.entities.pac.system.*;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gameplay.hunt.*;
import de.amr.pacmanfx.core.gamestate.EntityUpdater;
import de.amr.pacmanfx.core.model.GhostPersonality;

public class GameSystems {

    private final EntityUpdater entityUpdater = new EntityUpdater();
    private final ActorSpriteAnimController actorSpriteAnimController = new ActorSpriteAnimController();

    protected LifetimeSystem lifetime = new LifetimeSystem();
    protected MovementSystem motor =  new MovementSystem();
    protected WorldNavigationSystem navigator = new WorldNavigationSystem();
    protected RoamingSystem roamingNavigator = new RoamingSystem(navigator);

    protected WorldMovementPolicy<Pac> pacWorldMovementPolicy;
    protected PacAutoSteeringSystem pacAutoSteering;
    protected PacStateSystem pacState;
    protected PacPowerSystem pacPower;
    protected PacDigestionSystem pacDigestion;
    protected PacAnimationSystem pacAnimation;

    protected GhostStateSystem ghostState;
    protected GhostHuntingSystem ghostHuntingSystem;
    protected GhostHouseAccessSystem ghostHouseAccess;
    protected GhostWorldMovementPolicy ghostWorldMovementPolicy;
    protected GhostAnimationSystem ghostSpriteAnimation;

    protected GhostHuntingStrategy orangeGhostPokeyHuntingStrategy;
    protected GhostHuntingStrategy cyanGhostBashfulHuntingStrategy;
    protected GhostHuntingStrategy redGhostShadowHuntingStrategy;
    protected GhostHuntingStrategy pinkGhostSpeedyHuntingStrategy;

    protected BonusStateSystem bonusStateSystem;
    protected WorldMovementPolicy<Bonus> bonusWorldMovementPolicy;
    protected BonusMoveAndJumpSystem bonusMoveAndJumpSystem;

    // HUD systems
    protected ScoreSystem scoreSystem;
    protected LevelCounterSystem levelCounterSystem;
    protected LivesCounterSystem livesCounterSystem;

    public GameSystems() {
        createPacSystems();
        createGhostSystems();
        createBonusSystems();
        createHUDSystems();
    }

    protected void createPacSystems() {
        pacPower = new PacPowerSystem();
        pacDigestion = new PacDigestionSystem();
        pacWorldMovementPolicy = new PacWorldMovementPolicy();
        pacAutoSteering = new PacAutoSteeringSystem();
        pacState = new PacStateSystem();
        pacAnimation = new PacAnimationSystem();
    }

    protected void createGhostSystems() {
        ghostWorldMovementPolicy = new GhostWorldMovementPolicy();
        ghostState = new GhostStateSystem();

        redGhostShadowHuntingStrategy = createShadowHuntingStrategy();
        pinkGhostSpeedyHuntingStrategy = createSpeedyHuntingStrategy();
        cyanGhostBashfulHuntingStrategy = createBashfulHuntingStrategy();
        orangeGhostPokeyHuntingStrategy = createPokeyHuntingStrategy();

        ghostHouseAccess = new GhostHouseAccessSystem(navigator, ghostWorldMovementPolicy, motor);
        ghostHuntingSystem = new GhostHuntingSystem();

        ghostSpriteAnimation = new GhostAnimationSystem();
    }

    protected void createBonusSystems() {
        bonusWorldMovementPolicy = new BonusWorldMovementPolicy();
        bonusMoveAndJumpSystem = new BonusMoveAndJumpSystem(navigator, bonusWorldMovementPolicy);
        bonusStateSystem = new BonusStateSystem();
    }

    protected void createHUDSystems() {
        scoreSystem = new ScoreSystem();
        levelCounterSystem = new LevelCounterSystem();
        livesCounterSystem = new LivesCounterSystem();
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

    public EntityUpdater entityUpdater() {
        return entityUpdater;
    }

    public ActorSpriteAnimController actorSpriteAnimController() {
        return actorSpriteAnimController;
    }

    public LifetimeSystem lifetime() {
        return lifetime;
    }

    public MovementSystem motor() {
        return motor;
    }

    public WorldNavigationSystem worldNavigator() {
        return navigator;
    }

    public RoamingSystem roaming() {
        return roamingNavigator;
    }

    public PacAutoSteeringSystem pacAutoSteering() {
        return pacAutoSteering;
    }

    public PacStateSystem pacState() {
        return pacState;
    }

    public PacPowerSystem pacPower() {
        return pacPower;
    }

    public PacDigestionSystem pacDigestion() {
        return pacDigestion;
    }

    public WorldMovementPolicy<Pac> pacWorldMovementPolicy() {
        return pacWorldMovementPolicy;
    }

    public PacAnimationSystem pacAnimation() {
        return pacAnimation;
    }

    public GhostStateSystem ghostState() {
        return ghostState;
    }

    public GhostHouseAccessSystem ghostHouseAccess() {
        return ghostHouseAccess;
    }

    public GhostHuntingSystem ghostHuntingSystem() {
        return ghostHuntingSystem;
    }

    public GhostWorldMovementPolicy ghostWorldMovementPolicy() {
        return ghostWorldMovementPolicy;
    }

    public GhostHuntingStrategy ghostHuntingStrategy(GhostPersonality personality) {
        return switch (personality) {
            case RED_GHOST_SHADOW -> redGhostShadowHuntingStrategy;
            case PINK_GHOST_SPEEDY -> pinkGhostSpeedyHuntingStrategy;
            case CYAN_GHOST_BASHFUL -> cyanGhostBashfulHuntingStrategy;
            case ORANGE_GHOST_POKEY -> orangeGhostPokeyHuntingStrategy;
        };
    }

    public GhostAnimationSystem ghostAnimation() {
        return ghostSpriteAnimation;
    }

    public BonusStateSystem bonusState() {
        return bonusStateSystem;
    }

    public WorldMovementPolicy<Bonus> bonusWorldMovementPolicy() {
        return bonusWorldMovementPolicy;
    }

    public BonusMoveAndJumpSystem bonusMoveAndJump() {
        return bonusMoveAndJumpSystem;
    }

    // HUD systems

    public ScoreSystem scoreSystem() {
        return scoreSystem;
    }

    public LevelCounterSystem levelCounterSystem() {
        return levelCounterSystem;
    }

    public LivesCounterSystem livesCounterSystem() {
        return livesCounterSystem;
    }
}
