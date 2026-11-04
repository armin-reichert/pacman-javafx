/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.rules.GameRules;

import static java.util.Objects.requireNonNull;

/**
 * Context passed to game scenes and game flow state machines for the currently running game variant.
 */
public class GameContext {

    private final GameVariantRuntime runtime;

    private final CoinMechanism coinMechanism;

    private GameSession session;

    public GameContext(GameVariantRuntime runtime, CoinMechanism coinMechanism) {
        this.runtime = requireNonNull(runtime);
        this.coinMechanism = requireNonNull(coinMechanism);
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public GameSession session() {
        return session;
    }

    public GameVariantRuntime runtime() {
        return runtime;
    }

    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    public AbstractGameState state() {
        return runtime.gameFlow().state();
    }

    public GameFlow flow() {
        return runtime.gameFlow();
    }

    public GamePlay play() {
        return runtime.gamePlay();
    }

    public GameRules rules() {
        return runtime.gameRules();
    }

    public GameSystems systems() {
        return runtime.systems();
    }
}
