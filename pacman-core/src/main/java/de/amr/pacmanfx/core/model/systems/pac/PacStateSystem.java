/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.pac;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.comp.pac.PacDigestionComp;
import de.amr.pacmanfx.core.model.comp.pac.PacState;
import de.amr.pacmanfx.core.model.entities.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.steering.Steering;

import static java.util.Objects.requireNonNull;

public class PacStateSystem {

    private final WorldNavigationSystem navigator;
    private final WorldMovementPolicy movementPolicy;
    private final PacDigestionSystem digestionSystem;
    private final PacPowerSystem powerSystem;

    private Steering<Pac> automaticSteering;

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
        update(gameContext.cheats(), level, level.entities().pac());
    }

    //TODO This does not belong here
    public void setAutomaticSteering(Steering<Pac> steering) {
        automaticSteering = requireNonNull(steering);
    }

    private void update(
        GameCheats cheats,
        GameLevel level,
        Pac pac)
    {
        final PacState state = pac.state(); //TODO entity component
        final PacDigestionComp digestion = pac.digestion();
        final ActorSpeedRules speedRules = level.gameModel().rules().actorSpeedRules();

        if (state == PacState.DEAD || digestion.restingTicks() == PacDigestionComp.REST_FOREVER) {
            return;
        }

        digestionSystem.update(pac);

        if (digestionSystem.mustRest(pac)) {
            return;
        }

        if (cheats.isPacUsingAutopilot() || level.isDemoLevel()) {
            automaticSteering.steer(pac, level);
        }

        final float speed = powerSystem.isPowerActive(pac)
            ? speedRules.pacSpeedWhenHasPower(level)
            : speedRules.pacSpeed(level);

        navigator.setSpeed(pac, speed);
        navigator.tryMovingOrTeleporting(pac, level, movementPolicy);
    }

    public boolean notBlocked(Pac pac) {
        return !(pac.movement().hasZeroVelocity() ||didNotMoveThroughWorld(pac));
    }

    private boolean didNotMoveThroughWorld(Pac pac) {
        return !pac.worldNavigation().info.moved;
    }
}
