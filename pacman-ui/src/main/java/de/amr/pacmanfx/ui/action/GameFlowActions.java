/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.test.TestStateID;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
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

    public GameFlowActions(Game game) {

        actionStartGame = new GameAction(game, "start_game") {
            @Override
            protected void doAction() {
                game.start();
            }
        };

        actionQuit = new GameAction(game, "quit") {
            @Override
            protected void doAction() {
                Logger.info("Call QUIT handler for {}", game.ui().viewManager().assertCurrentView());
                game.ui().viewManager().assertCurrentView().handleQuit(game);
            }
        };

        actionLetGameStateExpire = new GameAction(game, "let_game_state_expire") {
            @Override
            protected void doAction() {
                game.context().state().triggerTimeout();
            }
        };

        actionRestartIntro = new GameAction(game, "restart_intro") {
            @Override
            protected void doAction() {
                final GameContext gameContext = game.context();
                final GameState gameState = gameContext.state();

                if (gameState.id() instanceof TestStateID) {
                    gameState.onExit(gameContext);
                }

                game.stop();
                game.machine().clock().start();
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
