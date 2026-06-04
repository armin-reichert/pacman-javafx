/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;

public abstract class TestState implements State<GameContext> {

    protected final TickTimer timer = new TickTimer(getClass().getSimpleName());

    @Override
    public TickTimer timer() {
        return timer;
    }
}
