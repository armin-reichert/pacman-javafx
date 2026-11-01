/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.ui.EngineUIContext;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;

public class GameFlowActions {

    private final GameAction<EngineUIContext> actionStartGame;
    private final GameAction<EngineUIContext> actionQuit;
    private final GameAction<EngineUIContext> actionLetGameStateExpire;
    private final GameAction<EngineUIContext> actionRestartSession;

    private final Set<ActionKeyBinding<EngineUIContext>> bindings;

    public GameFlowActions() {

        actionStartGame = new GameAction<>("start_game") {
            @Override
            public void execute(EngineUIContext engine) {
                engine.lifeCycle().startSession();
            }
        };

        actionQuit = new GameAction<>("quit") {
            @Override
            public void execute(EngineUIContext engine) {
                engine.viewManager().currentView().onQuit();
            }
        };

        actionLetGameStateExpire = new GameAction<>("let_game_state_expire") {
            @Override
            public void execute(EngineUIContext engine) {
                engine.optCurrentGame().ifPresent(game -> game.state().triggerTimeout());
            }
        };

        // Copied from MAME to restart the "machine"
        actionRestartSession = new GameAction<>("restart_session") {
            @Override
            public void execute(EngineUIContext engine) {
                engine.lifeCycle().restartSession();
            }

            @Override
            public boolean isEnabled(EngineUIContext engine) {
                return engine.optCurrentGame().isPresent();
            }
        };

        bindings = Set.of(
            new ActionKeyBinding<>(actionRestartSession, bareKey(KeyCode.F3)),
            new ActionKeyBinding<>(actionQuit, bareKey(KeyCode.Q))
        );
    }

    public GameAction<EngineUIContext> actionLetGameStateExpire() {
        return actionLetGameStateExpire;
    }

    public GameAction<EngineUIContext> actionQuit() {
        return actionQuit;
    }

    public GameAction<EngineUIContext> actionRestartSession() {
        return actionRestartSession;
    }

    public GameAction<EngineUIContext> actionStartGame() {
        return actionStartGame;
    }

    public Set<ActionKeyBinding<EngineUIContext>> bindings() {
        return bindings;
    }
}
