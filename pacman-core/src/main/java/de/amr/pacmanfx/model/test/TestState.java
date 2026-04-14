/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;

public abstract class TestState implements State<Game> {

    protected final TickTimer timer = new TickTimer(getClass().getSimpleName());

    @Override
    public TickTimer timer() {
        return timer;
    }
}
