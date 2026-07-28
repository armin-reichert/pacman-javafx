/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component.pac;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.model.component.ActorComponent;

public class PacPower implements ActorComponent {

    private final TickTimer timer = new TickTimer("PacPower-Timer");

    @Override
    public void reset() {
        timer.stop();
        timer.reset(0);
    }

    public TickTimer timer() {
        return timer;
    }

    @Override
    public String toString() {
        return "PacPower{" +
            "timer=" + timer +
            '}';
    }
}
