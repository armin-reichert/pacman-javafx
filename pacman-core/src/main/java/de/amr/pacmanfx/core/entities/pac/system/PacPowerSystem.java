/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.pacmanfx.core.entities.pac.Pac;
import org.tinylog.Logger;

public final class PacPowerSystem {

    public void update(Pac pac) {
        pac.power().timer().doTick();
    }

    public void start(Pac pac, long ticks) {
        pac.power().timer().restartTicks(ticks);
        Logger.debug("Power timer activated, {} ticks ({0.00} sec)", ticks, ticks / 60f);
    }

    public void reset(Pac pac) {
        pac.power().reset();
    }
}
