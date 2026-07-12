/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.state.TimedGameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;

public class GameFlowActions {

    private final GameAction actionStartGame;
    private final GameAction actionQuit;
    private final GameAction actionLetGameStateExpire;
    private final GameAction actionRestartIntro;

    private final Set<ActionKeyBinding> bindings;

    public GameFlowActions(GameActionContext actionContext) {

        actionStartGame = new GameAction(actionContext, "start_game") {
            @Override
            protected void doAction() {
                actionContext.startGamePlay();
            }
        };

        actionQuit = new GameAction(actionContext, "quit") {
            @Override
            protected void doAction() {
                Logger.info("Call QUIT handler for {}", actionContext.ui().views().assertCurrentView());
                actionContext.ui().views().assertCurrentView().handleQuit(actionContext);
            }
        };

        actionLetGameStateExpire = new GameAction(actionContext, "let_game_state_expire") {
            @Override
            protected void doAction() {
                actionContext.gameContext().state().triggerTimeout();
            }
        };

        actionRestartIntro = new GameAction(actionContext, "restart_intro") {
            @Override
            protected void doAction() {
                final GameContext gameContext = actionContext.gameContext();
                final TimedGameState gameState = gameContext.state();

                if (gameState.id() instanceof TestStateID) {
                    gameState.onExit(gameContext);
                }

                actionContext.suspendGamePlay();
                actionContext.clock().start();
                gameContext.flow().restartState(GameStateID.GAME_INTRO);
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionStartGame, bareKey(KeyCode.F3)),
            new ActionKeyBinding(actionQuit, bareKey(KeyCode.Q))
        );
    }

    public GameAction actionLetGameStateExpire() {
        return actionLetGameStateExpire;
    }

    public GameAction actionQuit() {
        return actionQuit;
    }

    public GameAction actionRestartIntro() {
        return actionRestartIntro;
    }

    public GameAction actionStartGame() {
        return actionStartGame;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
