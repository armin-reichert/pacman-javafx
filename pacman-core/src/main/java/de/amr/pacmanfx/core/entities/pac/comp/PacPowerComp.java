/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.comp;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class PacPowerComp implements GameEntityComponent {

    private boolean fading;

    private boolean fadingStart;

    private boolean starts;

    private boolean ends;

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
        return timer.isRunning();
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

    public boolean starts() {
        return starts;
    }

    public void setStarts(boolean starts) {
        this.starts = starts;
    }

    public boolean ends() {
        return ends;
    }

    public void setEnds(boolean ends) {
        this.ends = ends;
    }

    public long ticksRemaining() {
        return timer.isRunning() ? timer.remainingTicks() : 0;
    }

    public long ticksTotal() {
        return timer.durationTicks();
    }

    @Override
    public String toString() {
        return "PacPowerComp{" +
            "fading=" + fading +
            ", fadingStart=" + fadingStart +
            ", timer=" + timer +
            '}';
    }
}
