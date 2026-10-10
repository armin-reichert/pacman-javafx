/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
import de.amr.pacmanfx.ui.GameUI;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;

public class GameFlowActions {

    private final GameAction<GameEngineContext> actionStartGame;
    private final GameAction<GameEngineContext> actionQuit;
    private final GameAction<GameEngineContext> actionLetGameStateExpire;
    private final GameAction<GameEngineContext> actionRestartIntro;

    private final Set<ActionKeyBinding> bindings;

    public GameFlowActions() {

        actionStartGame = new GameAction<>("start_game") {
            @Override
            public void execute(GameEngineContext context) {
                context.engineLife().startGame();
            }
        };

        actionQuit = new GameAction<>("quit") {
            @Override
            public void execute(GameEngineContext context) {
                final GameUI ui = context.ui();
                Logger.info("Call QUIT handler for {}:", ui.viewManager().assertCurrentView());
                ui.viewManager().assertCurrentView().onQuit();
            }
        };

        actionLetGameStateExpire = new GameAction<>("let_game_state_expire") {
            @Override
            public void execute(GameEngineContext engineContext) {
                engineContext.optCurrentGame().ifPresent(game -> game.state().triggerTimeout());
            }
        };

        actionRestartIntro = new GameAction<>("restart_intro") {
            @Override
            public void execute(GameEngineContext engineContext) {
                engineContext.optCurrentGame().ifPresent(game -> {
                    final AbstractGameState gameState = game.state();

                    if (gameState.id() instanceof TestStateID) {
                        gameState.onExit(game);
                    }

                    engineContext.engineLife().suspendGame();
                    engineContext.clock().start();
                    game.playConfig().gameFlow().restartGameState(game, CommonGameStateID.GAME_INTRO);
                });
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionStartGame, bareKey(KeyCode.F3)),
            new ActionKeyBinding(actionQuit, bareKey(KeyCode.Q))
        );
    }

    public GameAction<GameEngineContext> actionLetGameStateExpire() {
        return actionLetGameStateExpire;
    }

    public GameAction<GameEngineContext> actionQuit() {
        return actionQuit;
    }

    public GameAction<GameEngineContext> actionRestartIntro() {
        return actionRestartIntro;
    }

    public GameAction<GameEngineContext> actionStartGame() {
        return actionStartGame;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
