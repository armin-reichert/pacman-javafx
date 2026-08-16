/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacPowerComp;
import org.tinylog.Logger;

public final class PacPowerSystem {

    public void start(Pac pac, long ticks) {
        pac.power().timer().restartTicks(ticks);
        Logger.debug("Power timer activated, {} ticks ({0.00} sec)", ticks, ticks / 60f);
    }

    public void reset(Pac pac) {
        pac.power().reset();
    }

    public void update(Pac pac, float fadingSeconds) {
        final PacPowerComp power = pac.power();
        final TickTimer timer = power.timer();
        if (timer.isRunning()) {
            final long fadingTicks = TickTimer.secToTicks(fadingSeconds);
            final boolean fading = timer.remainingTicks() <= fadingTicks;
            final boolean fadingStart = timer.durationTicks() < fadingTicks
                ? timer.tickCount() == 1
                : timer.remainingTicks() == fadingTicks;

            //TODO This is redundant and should be removed
            power.setActive(true);
            power.setFadingStart(fadingStart);
            power.setFading(fading);

            timer.doTick();
        }
        else {
            power.setActive(false);
            power.setFadingStart(false);
            power.setFading(false);
        }
    }
}
