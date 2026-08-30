/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.rules.GameRules;

import static java.util.Objects.requireNonNull;

public class PacUpdateSystem {

    private final MovementSystem motor;
    private final WorldNavigationSystem navigator;
    private final PacStateSystem pacStateSystem;
    private final PacDigestionSystem pacDigestionSystem;
    private final PacPowerSystem pacPowerSystem;
    private final PacAutoSteeringSystem pacAutoSteeringSystem;
    private final PacAnimationSystem pacAnimationSystem;
    private final ActorSpriteAnimController animController;
    private final WorldMovementPolicy<Pac> movementPolicy;

    public PacUpdateSystem(
        PacStateSystem pacStateSystem,
        PacDigestionSystem pacDigestionSystem,
        PacPowerSystem pacPowerSystem,
        PacAutoSteeringSystem pacAutoSteeringSystem,
        PacAnimationSystem pacAnimationSystem,
        ActorSpriteAnimController animController,
        WorldMovementPolicy<Pac> movementPolicy,
        WorldNavigationSystem navigator,
        MovementSystem motor)
    {
        this.pacStateSystem = requireNonNull(pacStateSystem);
        this.pacDigestionSystem = requireNonNull(pacDigestionSystem);
        this.pacPowerSystem = requireNonNull(pacPowerSystem);
        this.pacAutoSteeringSystem = requireNonNull(pacAutoSteeringSystem);
        this.pacAnimationSystem = requireNonNull(pacAnimationSystem);
        this.animController = requireNonNull(animController);
        this.movementPolicy = requireNonNull(movementPolicy);
        this.navigator = requireNonNull(navigator);
        this.motor = requireNonNull(motor);
    }

    public void update(GameContext game, GameLevel level, Pac pac) {
        final GameRules rules = game.variant().rules();
        final GameSession session = game.session();

        switch (pac.state().enumValue()) {
            case SLEEPING, DEAD -> navigator.setDisabled(pac, true);
            case ACTIVE -> navigator.setDisabled(pac, false);
        }

        final ActorSpeedRules speedRules = rules.actorSpeedRules();
        final float speed = pac.power().isActive()
            ? speedRules.pacSpeedWhenHasPower(game, level)
            : speedRules.pacSpeed(game, level);

        pacStateSystem.update(pac);
        pacDigestionSystem.update(pac);
        pacPowerSystem.update(pac, rules.pacPowerFadingSeconds(level.number()));

        // Steering and movement
        pacAutoSteeringSystem.update(session, pac);
        navigator.setMoveDirSpeed(pac, speed);
        navigator.tryMovingOrTeleporting(level, pac, motor, movementPolicy);

        // Animation
        pacAnimationSystem.update(pac);
        animController.select(pac, pac.animation().animationID());
        if (pac.animation().isDisabled()) {
            animController.stopSelected(pac);
        } else {
            animController.playSelected(pac);
        }
    }
}
