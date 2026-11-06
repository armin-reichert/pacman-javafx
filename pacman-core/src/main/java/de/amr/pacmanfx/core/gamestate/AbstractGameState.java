/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.Named;
import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;

import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractGameState implements State<GameContext>, Named {

    private final Named id;
    private final TickTimer timer;

    public AbstractGameState(Named id) {
        this.id = Objects.requireNonNull(id);
        this.timer = new TickTimer("GameStateTimer-" + getClass().getSimpleName());
    }

    public abstract void doEnterState(GameContext game);

    public abstract void doUpdate(GameContext game, long globalTickCount, long stateTickCount);

    public Named id() {
        return id;
    }

    public boolean nameIsOneOf(Named... names) {
        return Arrays.asList(names).contains(id);
    }

    @Override
    public String name() {
        return id.name();
    }

    @Override
    public TickTimer stateTimer() {
        return timer;
    }

    @Override
    public final void onEnter(GameContext game) {
        doEnterState(game);
    }

    @Override
    public final void onUpdate(GameContext game) {
        doUpdate(game, game.session().thisFrame().tickCount(), stateTimer().tickCount());
    }
}
