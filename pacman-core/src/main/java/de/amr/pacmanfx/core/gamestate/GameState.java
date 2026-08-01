/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.Identifier;
import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;

import java.util.Arrays;
import java.util.Objects;

public abstract class GameState implements State<GameContext>, Identifier {

    private final Identifier id;
    private final TickTimer timer;

    public GameState(Identifier id) {
        this.id = Objects.requireNonNull(id);
        this.timer = new TickTimer("GameStateTimer-" + getClass().getSimpleName());
    }

    public Identifier id() {
        return id;
    }

    public boolean isOneOf(Identifier... options) {
        return Arrays.asList(options).contains(id);
    }

    @Override
    public String name() {
        return id.name();
    }

    @Override
    public TickTimer timer() {
        return timer;
    }
}
