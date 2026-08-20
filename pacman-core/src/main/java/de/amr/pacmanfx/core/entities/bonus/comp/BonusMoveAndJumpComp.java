/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.comp;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.ecs.EntityComponent;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.steering.RouteGuidedSteering;

public class BonusMoveAndJumpComp implements EntityComponent {

    private static final int JUMP_PULSE_TICKS = 10;

    private final Pulse jumpPulse;

    private RouteGuidedSteering<Bonus> routeNavigation;

    private boolean targetReached;

    public BonusMoveAndJumpComp() {
        jumpPulse = new Pulse(JUMP_PULSE_TICKS, Pulse.State.OFF);
    }

    public Pulse jumpPulse() {
        return jumpPulse;
    }

    public void setRouteNavigation(RouteGuidedSteering<Bonus> routeNavigation) {
        this.routeNavigation = routeNavigation;
    }

    public RouteGuidedSteering<Bonus> routeNavigation() {
        return routeNavigation;
    }

    public void setTargetReached(boolean targetReached) {
        this.targetReached = targetReached;
    }

    public boolean targetReached() {
        return targetReached;
    }

    @Override
    public void reset() {
        targetReached = false;
    }
}
