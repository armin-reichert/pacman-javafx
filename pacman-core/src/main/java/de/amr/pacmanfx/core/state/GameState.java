/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.state;

import de.amr.basics.Identifier;
import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a game state like "booting", "intro", "playing" etc. Each game state has an internal tick timer.
 */
public class GameState implements State<GameContext>, Identifier {

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
