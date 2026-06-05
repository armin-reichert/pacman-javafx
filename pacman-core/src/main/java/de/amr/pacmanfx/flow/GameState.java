/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.flow;

import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import org.tinylog.Logger;

import java.util.Objects;

public class GameState implements State<GameContext> {

    private final String name;
    private final TickTimer timer;

    public GameState(String name) {
        this.name = Objects.requireNonNull(name);
        this.timer = new TickTimer("GameStateTimer-" + getClass().getSimpleName());
    }

    public GameState(GameStateIdentifier id) {
        this.name = Objects.requireNonNull(id).name();
        this.timer = new TickTimer("GameStateTimer-" + getClass().getSimpleName());
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void onEnter(GameContext context) {
        Logger.trace("onEnter");
    }

    @Override
    public void onUpdate(GameContext context) {
        Logger.trace("onUpdate");
    }

    @Override
    public void onExit(GameContext context) {
        Logger.trace("onExit");
    }

    @Override
    public TickTimer timer() {
        return timer;
    }

}
