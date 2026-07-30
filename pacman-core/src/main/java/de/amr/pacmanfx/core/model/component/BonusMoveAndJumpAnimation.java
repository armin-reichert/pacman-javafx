/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.model.actors.Bonus;
import de.amr.pacmanfx.core.steering.RouteGuidedActorSteering;

public class BonusMoveAndJumpAnimation implements ActorComponent {

    private static final int PULSE_CHANGE_TICKS = 10;

    private final Pulse pulse;
    private RouteGuidedActorSteering<Bonus> routeNavigation;
    private boolean targetReached;

    public BonusMoveAndJumpAnimation() {
        pulse = new Pulse(PULSE_CHANGE_TICKS, Pulse.State.OFF);
    }

    public Pulse pulse() {
        return pulse;
    }

    public void setRouteNavigation(RouteGuidedActorSteering<Bonus> routeNavigation) {
        this.routeNavigation = routeNavigation;
    }

    public RouteGuidedActorSteering<Bonus> routeNavigation() {
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
