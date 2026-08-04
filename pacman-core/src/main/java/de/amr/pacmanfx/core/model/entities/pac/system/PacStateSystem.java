/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.pac.system;

import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.entities.pac.PacState;
import de.amr.pacmanfx.core.model.entities.pac.comp.PacDigestionComp;
import de.amr.pacmanfx.core.model.entities.pac.comp.PacPowerComp;
import de.amr.pacmanfx.core.model.entities.pac.comp.PacStateComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.ActorSpeedRules;

import static java.util.Objects.requireNonNull;

public class PacStateSystem {

    private final WorldNavigationSystem navigator;
    private final WorldMovementPolicy movementPolicy;
    private final PacDigestionSystem digestionSystem;

    public PacStateSystem(
        WorldNavigationSystem navigator,
        WorldMovementPolicy movementPolicy,
        PacDigestionSystem digestionSystem)
    {
        this.navigator = navigator;
        this.movementPolicy = movementPolicy;
        this.digestionSystem = digestionSystem;
    }

    public void setState(Pac pac, PacState pacState) {
        requireNonNull(pac);
        requireNonNull(pacState);
        pac.stateComp().setState(pacState);
    }

    public void update(Pac pac, GameLevel level) {
        final PacStateComp state = pac.stateComp();
        final PacDigestionComp digestion = pac.digestion();
        final PacPowerComp power = pac.power();

        final ActorSpeedRules speedRules = level.gameModel().rules().actorSpeedRules();

        digestionSystem.update(pac);

        switch (state.pacState()) {
            case ACTIVE -> {
                state.setMoving(notBlocked(pac));
            }
            case DEAD -> {
                state.setMoving(false);
            }
        }

        if (digestion.restingTicks() == PacDigestionComp.REST_FOREVER || digestion.restingTicks() > 0) {
            return;
        }

        final float speed = power.isPowerActive() ? speedRules.pacSpeedWhenHasPower(level) : speedRules.pacSpeed(level);

        navigator.setSpeed(pac, speed);
        navigator.tryMovingOrTeleporting(pac, level, movementPolicy);
    }

    public boolean notBlocked(Pac pac) {
        final MovementComp movement = pac.movement();
        return !(movement.hasZeroVelocity() ||didNotMoveThroughWorld(pac));
    }

    private boolean didNotMoveThroughWorld(Pac pac) {
        final WorldNavigationComp worldNavigation = pac.worldNavigation();
        return !worldNavigation.info.moved;
    }
}
