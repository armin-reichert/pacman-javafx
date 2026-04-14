/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.lib.fsm;

import de.amr.pacmanfx.lib.TickTimer;

public abstract class TimeControlledState<C> implements State<C> {

    protected final TickTimer timer = new TickTimer("Timer-" + name());

    @Override
    public TickTimer timer() {
        return timer;
    }
}
