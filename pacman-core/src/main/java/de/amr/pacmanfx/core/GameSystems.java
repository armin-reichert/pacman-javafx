/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.ecs.systems.*;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.system.BonusMoveAndJumpSystem;
import de.amr.pacmanfx.core.entities.bonus.system.BonusStateSystem;
import de.amr.pacmanfx.core.entities.bonus.system.BonusUpdateSystem;
import de.amr.pacmanfx.core.entities.bonus.system.BonusWorldMovementPolicy;
import de.amr.pacmanfx.core.entities.ghost.system.*;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.pac.system.*;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gameplay.hunt.*;
import de.amr.pacmanfx.core.gamestate.EntityUpdateSystem;
import de.amr.pacmanfx.core.gamestate.HUD_UpdateSystem;
import de.amr.pacmanfx.core.model.GhostPersonality;

public class GameSystems {

    private final EntityUpdateSystem entityUpdateSystem = new EntityUpdateSystem();
    private final HUD_UpdateSystem hudUpdateSystem = new HUD_UpdateSystem();

    private final ActorSpriteAnimController actorSpriteAnimController = new ActorSpriteAnimController();

    protected LifetimeSystem lifetime = new LifetimeSystem();
    protected MovementSystem motor =  new MovementSystem();
    protected WorldNavigationSystem navigator = new WorldNavigationSystem(motor);
    protected RoamingSystem roaming = new RoamingSystem(navigator);

    protected WorldMovementPolicy<Pac> pacWorldMovementPolicy;
    protected PacAutoSteeringSystem pacAutoSteeringSystem;

    protected PacUpdateSystem pacUpdateSystem;
    protected PacStateSystem pacStateSystem;
    protected PacPowerSystem pacPowerSystem;
    protected PacDigestionSystem pacDigestionSystem;
    protected PacAnimationSystem pacAnimationSystem;

    protected GhostUpdateSystem ghostUpdateSystem;
    protected GhostStateSystem ghostStateSystem;
    protected GhostHuntingSystem ghostHuntingSystem;
    protected GhostHouseAccessSystem ghostHouseAccessSystem;
    protected GhostWorldMovementPolicy ghostWorldMovementPolicy;
    protected GhostAnimationSystem ghostAnimationSystem;

    protected GhostHuntingStrategy orangeGhostPokeyHuntingStrategy;
    protected GhostHuntingStrategy cyanGhostBashfulHuntingStrategy;
    protected GhostHuntingStrategy redGhostShadowHuntingStrategy;
    protected GhostHuntingStrategy pinkGhostSpeedyHuntingStrategy;

    protected BonusUpdateSystem bonusUpdateSystem;
    protected BonusStateSystem bonusStateSystem;
    protected WorldMovementPolicy<Bonus> bonusWorldMovementPolicy;
    protected BonusMoveAndJumpSystem bonusMoveAndJumpSystem;

    // HUD systems
    protected ScoreSystem scoreSystem;
    protected LevelCounterSystem levelCounterSystem;

    public GameSystems() {
        createPacSystems();
        createGhostSystems();
        createBonusSystems();
        createHUDSystems();
    }

    protected void createPacSystems() {
        pacWorldMovementPolicy = new PacWorldMovementPolicy();

        pacAutoSteeringSystem = new PacAutoSteeringSystem();
        pacPowerSystem = new PacPowerSystem();
        pacDigestionSystem = new PacDigestionSystem();
        pacStateSystem = new PacStateSystem();
        pacAnimationSystem = new PacAnimationSystem();

        pacUpdateSystem = new PacUpdateSystem(
            pacStateSystem,
            pacDigestionSystem,
            pacPowerSystem,
            pacAutoSteeringSystem,
            pacAnimationSystem,
            actorSpriteAnimController,
            pacWorldMovementPolicy,
            navigator);
    }

    protected void createGhostSystems() {
        ghostWorldMovementPolicy = new GhostWorldMovementPolicy();

        redGhostShadowHuntingStrategy = createShadowHuntingStrategy();
        pinkGhostSpeedyHuntingStrategy = createSpeedyHuntingStrategy();
        cyanGhostBashfulHuntingStrategy = createBashfulHuntingStrategy();
        orangeGhostPokeyHuntingStrategy = createPokeyHuntingStrategy();

        ghostStateSystem = new GhostStateSystem();
        ghostHouseAccessSystem = new GhostHouseAccessSystem(navigator, ghostWorldMovementPolicy, motor);
        ghostHuntingSystem = new GhostHuntingSystem();
        ghostAnimationSystem = new GhostAnimationSystem(actorSpriteAnimController);

        ghostUpdateSystem = new GhostUpdateSystem(
            ghostHouseAccessSystem,
            ghostHuntingSystem,
            ghostStateSystem,
            ghostAnimationSystem,
            actorSpriteAnimController
        );
    }

    protected void createBonusSystems() {
        bonusWorldMovementPolicy = new BonusWorldMovementPolicy();

        bonusStateSystem = new BonusStateSystem();
        bonusMoveAndJumpSystem = new BonusMoveAndJumpSystem(navigator, bonusWorldMovementPolicy);
        bonusUpdateSystem = new BonusUpdateSystem(bonusStateSystem, bonusMoveAndJumpSystem);
    }

    protected void createHUDSystems() {
        scoreSystem = new ScoreSystem();
        levelCounterSystem = new LevelCounterSystem();
    }

    // Global systems

    public EntityUpdateSystem updateSystem() {
        return entityUpdateSystem;
    }

    public HUD_UpdateSystem hudUpdateSystem() {
        return hudUpdateSystem;
    }

    public LifetimeSystem lifetime() {
        return lifetime;
    }

    public MovementSystem motor() {
        return motor;
    }

    public WorldNavigationSystem navigator() {
        return navigator;
    }

    public RoamingSystem roaming() {
        return roaming;
    }

    public ActorSpriteAnimController actorSpriteAnimController() {
        return actorSpriteAnimController;
    }


    // Pac-Man systems

    public PacUpdateSystem pacUpdateSystem() {
        return pacUpdateSystem;
    }

    public PacAutoSteeringSystem pacAutoSteering() {
        return pacAutoSteeringSystem;
    }

    public PacStateSystem pacState() {
        return pacStateSystem;
    }

    public PacPowerSystem pacPower() {
        return pacPowerSystem;
    }

    public PacDigestionSystem pacDigestion() {
        return pacDigestionSystem;
    }

    public PacAnimationSystem pacAnimation() {
        return pacAnimationSystem;
    }

    // Pac-man policies

    public WorldMovementPolicy<Pac> pacWorldMovementPolicy() {
        return pacWorldMovementPolicy;
    }

    // Ghost systems


    public GhostUpdateSystem ghostUpdateSystem() {
        return ghostUpdateSystem;
    }

    public GhostStateSystem ghostState() {
        return ghostStateSystem;
    }

    public GhostHouseAccessSystem ghostHouseAccess() {
        return ghostHouseAccessSystem;
    }

    public GhostHuntingSystem ghostHuntingSystem() {
        return ghostHuntingSystem;
    }

    public GhostAnimationSystem ghostAnimation() {
        return ghostAnimationSystem;
    }

    // Ghost policies/strategies

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

    // Bonus systems


    public BonusUpdateSystem bonusUpdateSystem() {
        return bonusUpdateSystem;
    }

    public BonusStateSystem bonusState() {
        return bonusStateSystem;
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
}
