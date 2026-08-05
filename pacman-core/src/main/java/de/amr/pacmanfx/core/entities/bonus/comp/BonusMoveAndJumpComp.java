/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.comp;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.steering.RouteGuidedSteering;

public class BonusMoveAndJumpComp implements GameEntityComponent {

    private static final int PULSE_CHANGE_TICKS = 10;

    private final Pulse pulse;
    private RouteGuidedSteering routeNavigation;
    private boolean targetReached;

    public BonusMoveAndJumpComp() {
        pulse = new Pulse(PULSE_CHANGE_TICKS, Pulse.State.OFF);
    }

    public Pulse pulse() {
        return pulse;
    }

    public void setRouteNavigation(RouteGuidedSteering routeNavigation) {
        this.routeNavigation = routeNavigation;
    }

    public RouteGuidedSteering routeNavigation() {
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
