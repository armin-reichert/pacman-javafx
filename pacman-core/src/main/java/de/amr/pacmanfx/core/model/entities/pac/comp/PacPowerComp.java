/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.pac.comp;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.model.level.GameLevel;

public class PacPowerComp implements GameEntityComponent {

    private final TickTimer timer = new TickTimer("PacPower-Timer");

    @Override
    public void reset() {
        timer.stop();
        timer.reset(0);
    }

    public TickTimer timer() {
        return timer;
    }

    public boolean isPowerActive() {
        return timer.isRunning();
    }

    public boolean isPowerOver() {
        return timer.hasExpired();
    }

    public boolean isPowerFading(GameLevel level) {
        long fadingTicks = TickTimer.secToTicks(level.pacPowerFadingSeconds());
        return timer.isRunning() && timer.remainingTicks() <= fadingTicks;
    }

    public boolean isPowerStartingFading(GameLevel level) {
        long fadingTicks = TickTimer.secToTicks(level.pacPowerFadingSeconds());
        return timer.isRunning() && timer.remainingTicks() == fadingTicks
            || timer.durationTicks() < fadingTicks && timer.tickCount() == 1;
    }

    public long powerTicksRemaining() {
        return timer.isRunning() ? timer.remainingTicks() : 0;
    }

    public long powerTicksTotal() {
        return timer.durationTicks();
    }

    @Override
    public String toString() {
        return "PacPower{" +
            "timer=" + timer +
            '}';
    }
}
