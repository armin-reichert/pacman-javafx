/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.pac.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.entities.pac.comp.PacDigestionComp;
import de.amr.pacmanfx.core.model.entities.pac.PacState;
import de.amr.pacmanfx.core.model.entities.pac.comp.PacStateComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.ActorSpeedRules;

import static java.util.Objects.requireNonNull;

public class PacStateSystem {

    private final WorldNavigationSystem navigator;
    private final WorldMovementPolicy movementPolicy;
    private final PacDigestionSystem digestionSystem;
    private final PacPowerSystem powerSystem;

    public PacStateSystem(
        WorldNavigationSystem navigator,
        WorldMovementPolicy movementPolicy,
        PacDigestionSystem digestionSystem,
        PacPowerSystem powerSystem)
    {
        this.navigator = navigator;
        this.movementPolicy = movementPolicy;
        this.digestionSystem = digestionSystem;
        this.powerSystem = powerSystem;
    }

    public void setState(Pac pac, PacState pacState) {
        requireNonNull(pac);
        requireNonNull(pacState);
        pac.stateComp().setState(pacState);
    }

    public void update(GameContext gameContext) {
        requireNonNull(gameContext);
        final GameLevel level = gameContext.assertLevel();
        update(level.entities().pac(), level);
    }

    private void update(GameEntity pac, GameLevel level) {
        final PacStateComp state = pac.requireComponent(PacStateComp.class);
        final PacDigestionComp digestion = pac.requireComponent(PacDigestionComp.class);

        final ActorSpeedRules speedRules = level.gameModel().rules().actorSpeedRules();

        if (state.pacState() == PacState.DEAD || digestion.restingTicks() == PacDigestionComp.REST_FOREVER) {
            return;
        }

        state.setMoving(state.pacState() == PacState.ACTIVE && notBlocked(pac));

        digestionSystem.update(pac);

        if (digestionSystem.mustRest(pac)) {
            return;
        }

        final float speed = powerSystem.isPowerActive(pac)
            ? speedRules.pacSpeedWhenHasPower(level)
            : speedRules.pacSpeed(level);

        navigator.setSpeed(pac, speed);
        navigator.tryMovingOrTeleporting(pac, level, movementPolicy);
    }

    public boolean notBlocked(GameEntity pac) {
        final MovementComp movement = pac.requireComponent(MovementComp.class);
        return !(movement.hasZeroVelocity() ||didNotMoveThroughWorld(pac));
    }

    private boolean didNotMoveThroughWorld(GameEntity pac) {
        final WorldNavigationComp worldNavigation = pac.requireComponent(WorldNavigationComp.class);
        return !worldNavigation.info.moved;
    }
}
