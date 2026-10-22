/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.engine.QuitHandler;
import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.ui.EngineUIContext;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;

public class GameFlowActions {

    private final GameAction<EngineUIContext> actionStartGame;
    private final GameAction<EngineUIContext> actionQuit;
    private final GameAction<EngineUIContext> actionLetGameStateExpire;
    private final GameAction<EngineUIContext> actionRestartIntro;

    private final Set<ActionKeyBinding<EngineUIContext>> bindings;

    public GameFlowActions() {

        actionStartGame = new GameAction<>("start_game") {
            @Override
            public void execute(EngineUIContext engine) {
                engine.lifeCycle().startGame();
            }
        };

        actionQuit = new GameAction<>("quit") {
            @Override
            public void execute(EngineUIContext engine) {
                engine.viewManager().optCurrentView().ifPresent(QuitHandler::onQuit);
            }
        };

        actionLetGameStateExpire = new GameAction<>("let_game_state_expire") {
            @Override
            public void execute(EngineUIContext engine) {
                engine.optCurrentGame().ifPresent(game -> game.state().triggerTimeout());
            }
        };

        actionRestartIntro = new GameAction<>("restart_intro") {
            @Override
            public void execute(EngineUIContext engine) {
                engine.optCurrentGame().ifPresent(game -> {
                    final AbstractGameState gameState = game.state();

                    if (gameState.id() instanceof TestStateID) {
                        gameState.onExit(game);
                    }

                    engine.lifeCycle().suspendGame();
                    game.playConfig().gameFlow().restartGameState(game, CommonGameStateID.GAME_INTRO);
                    engine.clock().start();
                });
            }
        };

        bindings = Set.of(
            new ActionKeyBinding<>(actionStartGame, bareKey(KeyCode.F3)),
            new ActionKeyBinding<>(actionQuit, bareKey(KeyCode.Q))
        );
    }

    public GameAction<EngineUIContext> actionLetGameStateExpire() {
        return actionLetGameStateExpire;
    }

    public GameAction<EngineUIContext> actionQuit() {
        return actionQuit;
    }

    public GameAction<EngineUIContext> actionRestartIntro() {
        return actionRestartIntro;
    }

    public GameAction<EngineUIContext> actionStartGame() {
        return actionStartGame;
    }

    public Set<ActionKeyBinding<EngineUIContext>> bindings() {
        return bindings;
    }
}
