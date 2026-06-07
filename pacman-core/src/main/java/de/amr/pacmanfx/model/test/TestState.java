/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;

public abstract class TestState extends GameState {

    protected final TickTimer timer = new TickTimer(getClass().getSimpleName());

    public TestState(String name) {
        super(name);
    }

    @Override
    public TickTimer timer() {
        return timer;
    }
}
