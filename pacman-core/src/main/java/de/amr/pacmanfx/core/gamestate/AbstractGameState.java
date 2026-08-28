/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.Named;
import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.rules.GameRules;

import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractGameState implements State<GameContext>, Named {

    private final Named id;
    private final TickTimer timer;

    // State context variables
    protected GameRules rules;
    protected GameFlowController flow;
    protected GamePlay gamePlay;
    protected GameSystems systems;
    protected GameSession session;
    protected HUD hud;

    public AbstractGameState(Named id) {
        this.id = Objects.requireNonNull(id);
        this.timer = new TickTimer("GameStateTimer-" + getClass().getSimpleName());
    }

    public abstract void onEnterState(GameContext game);

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
    public TickTimer timer() {
        return timer;
    }

    @Override
    public final void onEnter(GameContext game) {
        initStateContext(game);
        onEnterState(game);
    }

    protected void initStateContext(GameContext game) {
        rules = game.variant().rules();
        flow = game.variant().gameFlow();
        gamePlay = game.variant().gamePlay();
        systems = game.variant().systems();
        session = game.session();
        hud = session.hud();
    }
}
