/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
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

    public GameFlowActions() {

        actionStartGame = new GameAction("start_game") {
            @Override
            protected void doAction(GameAppContext app) {
                app.lifecycle().startPlaying();
            }
        };

        actionQuit = new GameAction("quit") {
            @Override
            protected void doAction(GameAppContext app) {
                Logger.info("Call QUIT handler for {}", app.ui().views().assertCurrentView());
                app.ui().views().assertCurrentView().handleQuit(app);
            }
        };

        actionLetGameStateExpire = new GameAction("let_game_state_expire") {
            @Override
            protected void doAction(GameAppContext app) {
                app.game().session().gameState().triggerTimeout();
            }
        };

        actionRestartIntro = new GameAction("restart_intro") {
            @Override
            protected void doAction(GameAppContext app) {
                final GameContext game = app.game();
                final GameState gameState = game.session().gameState();

                if (gameState.id() instanceof TestStateID) {
                    gameState.onExit(game);
                }

                app.lifecycle().suspendPlaying();
                app.clock().start();
                game.session().gameFlow().restartState(game, CommonGameStateID.GAME_INTRO);
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
