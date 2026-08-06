/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.comp;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class PacPowerComp implements GameEntityComponent {

    private boolean active;

    private boolean fading;

    private boolean fadingStart;

    private final TickTimer timer = new TickTimer("PacPower-Timer");

    @Override
    public void reset() {
        timer.stop();
        timer.reset(0);
    }

    public TickTimer timer() {
        return timer;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isFading() {
        return fading;
    }

    public void setFading(boolean fading) {
        this.fading = fading;
    }

    public boolean isFadingStart() {
        return fadingStart;
    }

    public void setFadingStart(boolean fadingStart) {
        this.fadingStart = fadingStart;
    }

    public boolean isOver() {
        return !active;
    }

    public long ticksRemaining() {
        return timer.isRunning() ? timer.remainingTicks() : 0;
    }

    public long ticksTotal() {
        return timer.durationTicks();
    }

    @Override
    public String toString() {
        return "PacPower{" +
            "timer=" + timer +
            '}';
    }
}
