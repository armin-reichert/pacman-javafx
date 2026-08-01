/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities;


import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.model.GameEntityComponent;

public class MarqueeTimerComp implements GameEntityComponent {

    private final TickTimer runner = new TickTimer("Marquee-Timer");

    public TickTimer runner() {
        return runner;
    }

    @Override
    public void reset() {
        runner.resetToIndefiniteDuration();
    }
}
