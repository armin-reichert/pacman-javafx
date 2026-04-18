/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;

public abstract class TestState<GAME extends Game> implements State<GAME> {

    protected final TickTimer timer = new TickTimer(getClass().getSimpleName());

    @Override
    public TickTimer timer() {
        return timer;
    }
}
