/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacPowerComp;
import org.tinylog.Logger;

public final class PacPowerSystem {

    public void start(Pac pac, long durationTicks) {
        pac.power().timer().restartTicks(durationTicks);
        Logger.debug("Power timer activated, {} ticks ({0.00} sec)", durationTicks, durationTicks / 60f);
    }

    public void stopAndReset(Pac pac) {
        pac.power().reset();
    }

    public void update(Pac pac, float fadingSeconds) {
        final PacPowerComp power = pac.power();
        final TickTimer timer = power.timer();

        if (!timer.isRunning()) {
            power.setStarts(false);
            power.setFadingStart(false);
            power.setFading(false);
            power.setEnds(false);
        }
        else {
            final boolean powerStarts = timer.tickCount() == 0;

            final long fadingTicks = TickTimer.secToTicks(fadingSeconds);

            final boolean powerStartsFading = timer.durationTicks() > fadingTicks
                ? timer.remainingTicks() == fadingTicks
                : timer.tickCount() == 0;

            final boolean powerFading = timer.remainingTicks() <= fadingTicks;

            final boolean powerEnds = powerFading && timer.remainingTicks() == 0;

            power.setStarts(powerStarts);
            power.setFadingStart(powerStartsFading);
            power.setFading(powerFading);
            power.setEnds(powerEnds);

            timer.doTick();
        }
    }
}
