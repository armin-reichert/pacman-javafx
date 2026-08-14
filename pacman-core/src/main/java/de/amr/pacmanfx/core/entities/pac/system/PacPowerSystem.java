/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacPowerComp;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.GameRules;
import org.tinylog.Logger;

public final class PacPowerSystem {

    public void update(GameRules rules, GameLevel level, Pac pac) {
        final PacPowerComp power = pac.power();
        final TickTimer timer = power.timer();

        timer.doTick();
        power.setActive(timer.isRunning());
        power.setFading(isPowerFading(rules, level, timer));
        power.setFadingStart(isPowerStartingFading(rules, level, timer));
    }

    private boolean isPowerFading(GameRules rules, GameLevel level, TickTimer timer) {
        long fadingTicks = TickTimer.secToTicks(rules.pacPowerFadingSeconds(level.number()));
        if (!timer.isRunning()) {
            return false;
        }
        return timer.remainingTicks() <= fadingTicks;
    }

    private boolean isPowerStartingFading(GameRules rules, GameLevel level, TickTimer timer) {
        long fadingTicks = TickTimer.secToTicks(rules.pacPowerFadingSeconds(level.number()));
        if (!timer.isRunning()) {
            return false;
        }
        if (timer.durationTicks() < fadingTicks) {
            return timer.tickCount() == 1;
        }
        return timer.remainingTicks() == fadingTicks;
    }

    public void start(Pac pac, long ticks) {
        pac.power().timer().restartTicks(ticks);
        Logger.debug("Power timer activated, {} ticks ({0.00} sec)", ticks, ticks / 60f);
    }

    public void reset(Pac pac) {
        pac.power().reset();
    }
}
