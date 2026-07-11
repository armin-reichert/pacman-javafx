package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.event.GameStateChangeEvent;
import de.amr.pacmanfx.core.flow.GameFlow;

public class TengenMsPacMan_GameFlow extends GameFlow {

    public TengenMsPacMan_GameFlow() {
        super("Tengen Ms. Pac-Man Game Flow");
        for (TengenMsPacMan_GameState gameState : TengenMsPacMan_GameState.values()) {
            addState(gameState.state());
        }
        addStateChangeListener((oldState, newState) ->
            context().eventManager().publishGameEvent(new GameStateChangeEvent(oldState, newState)));
    }
}
