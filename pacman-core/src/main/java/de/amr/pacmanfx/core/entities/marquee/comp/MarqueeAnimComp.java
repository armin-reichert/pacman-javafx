/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.marquee.comp;


import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class MarqueeAnimComp implements GameEntityComp {

    private final TickTimer runner = new TickTimer("Marquee-Timer");

    public TickTimer tickTimer() {
        return runner;
    }

    @Override
    public void reset() {
        runner.resetToIndefiniteDuration();
    }
}
